
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /home/gradle/project

# Kotlin/Wasm's tooling setup downloads Node.js 25 which is dynamically
# linked against libatomic.so.1. The base gradle image on linux/arm64
# doesn't ship that library, so :kotlinWasmToolingSetup fails with
#   error while loading shared libraries: libatomic.so.1: cannot open
#   shared object file ... Process 'Setup of tooling dependencies' returns 127
# Install it explicitly here. Drop once Kotlin ships a Node bundle
# without that runtime dep.
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends libatomic1 \
    && rm -rf /var/lib/apt/lists/*
USER gradle

#fake data to declare args
ARG ENV_MODE=dev
ARG API_BASE_URL=https://example.com
ARG PROD_PORT=443
ARG DEV_URL=localhost
ARG DEV_PORT=10000

ENV ENV_MODE=$ENV_MODE
ENV API_BASE_URL=$API_BASE_URL
ENV PROD_PORT=$PROD_PORT
ENV DEV_URL=$DEV_URL
ENV DEV_PORT=$DEV_PORT

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY composeApp/build.gradle.kts composeApp/
RUN gradle dependencies --no-daemon || true
COPY . .
RUN gradle :composeApp:wasmJsBrowserDistribution --no-daemon
FROM nginx:alpine
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

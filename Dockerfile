
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /home/gradle/project

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

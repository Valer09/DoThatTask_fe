
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /home/gradle/project

COPY . .

RUN gradle :composeApp:wasmJsBrowserDistribution --no-daemon

FROM nginx:alpine

COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

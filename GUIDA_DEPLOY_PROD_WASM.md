# Guide: Production Deployment — WASM via Docker

## 1. How environment configuration works (recap)

The build script (`composeApp/build.gradle.kts`) reads backend connection parameters in a cascade:

```
local.properties  →  System.getenv()  →  gradle.properties  →  default hardcoded
```

The `generateEnvironment` task produces `Environment.kt` (in the `build/` directory, never committed) with the values compiled into the WASM binary. The generated file is located at:

```
composeApp/build/generated/environment/commonMain/.../Config/Environment.kt
```

The key parameters are:

| Key in local.properties | Meaning |
|---|---|
| `ENV_MODE` | `"dev"` or `"prod"` — selects the dev/prod branch |
| `API_BASE_URL` | Full prod backend URL (e.g. `https://production.url`) |
| `PROD_PORT` | Prod backend port (e.g. `10000`) |
| `DEV_URL` | Dev backend IP/host (e.g. `localhost`) |
| `DEV_PORT` | Dev backend port |

---

## 2. How to pass production data to the Docker WASM build

### Premise

The `Dockerfile` should not copy everything — `local.properties` (with signing credentials and real URLs) should stay local for security, and intermediate layers of the Docker builder image should not contain `local.properties`.

Therefore, **add a `.dockerignore`**:

```
local.properties
gradle.properties.local
/composeApp/keystore.jks
/composeApp/release/
.gradle/
build/
.idea/
*.iml
```

---

### Approach A — Local build with `local.properties` (current, simplest)

**When to use:** manual builds from your machine, occasional deployments.

**Steps:**

1. Make sure `local.properties` has the production values:

   ```properties
   ENV_MODE=prod
   API_BASE_URL="https://prod.url.com"
   PROD_PORT=prod-port
   ```

2. Run the Docker build (for ARM64, e.g. Raspberry Pi):

   ```bash
   docker buildx create --use
   docker buildx build --platform linux/arm64 \
     -t <dockerregistry>:latest \
     --push .
   ```

   Example docker registry → `jack89/dothattask:latest`

3. On the production host:

   ```bash
   docker pull <dockerregistry>:latest
   docker-compose up -d
   ```

---

### Approach B — Docker build args (recommended for CI/CD and security)

**When to use:** CI/CD (GitHub Actions), automated builds, environments where you don't want `local.properties` on the build machine.

#### Step 1: modify the `Dockerfile` to accept build args

```dockerfile
FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /home/gradle/project

# Build args for the production environment
ARG ENV_MODE=prod
ARG API_BASE_URL=https://example.com
ARG PROD_PORT=443
ARG DEV_URL=localhost
ARG DEV_PORT=10000

# Export as environment variables (read by System.getenv() in the build script)
ENV ENV_MODE=$ENV_MODE
ENV API_BASE_URL=$API_BASE_URL
ENV PROD_PORT=$PROD_PORT
ENV DEV_URL=$DEV_URL
ENV DEV_PORT=$DEV_PORT

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true
COPY . .
RUN gradle :composeApp:wasmJsBrowserDistribution --no-daemon

FROM nginx:alpine
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### Step 2: build passing the production values

```bash
docker buildx build \
  --platform linux/arm64 \
  --build-arg ENV_MODE=prod \
  --build-arg API_BASE_URL=xxx \
  --build-arg PROD_PORT=xxx \
  -t <dockerregistry>:latest \
  --push .
```

**In GitHub Actions:**

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    platforms: linux/arm64
    push: true
    tags: <dockerregistry>:latest
    build-args: |
      ENV_MODE=prod
      API_BASE_URL=${{ secrets.API_BASE_URL_PROD }}
      PROD_PORT=${{ secrets.PROD_PORT }}
```

---

## 3. How the cascade works in the Dockerfile

When the Dockerfile uses `ENV ENV_MODE=$ENV_MODE`, Gradle reads it via `System.getenv("ENV_MODE")` — which is the second step of the cascade:

```
local.properties  →  System.getenv()  ←── value from --build-arg enters here
       ↓ (absent because it's in .dockerignore)
  gradle.properties (safe defaults, e.g. localhost)
```

So, **without any changes to `build.gradle.kts`**: as long as the env vars are exposed in the builder layer, the build script picks them up automatically.

---

## 4. Pre-deploy production checklist

- [ ] `local.properties` has `ENV_MODE="prod"` (Approach A) **or** `--build-arg` flags are used (Approach B)
- [ ] `API_BASE_URL` points to the correct backend
- [ ] `PROD_PORT` is set correctly
- [ ] `docker-compose.yml` has the correct port exposed

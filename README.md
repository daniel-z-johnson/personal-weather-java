# personal-weather

A personal weather app. The backend is Kotlin + Spring Boot, in `backend/`.

## Requirements

- Java 25
- Docker (for local services and for tests, which use Testcontainers)

## Running locally

1. Copy `example.env` to `.env` and fill in your OpenWeather API key. `.env` is gitignored.
2. Start Postgres and Redis:

   ```sh
   docker compose up -d postgres redis
   ```

3. Run the backend with the `local` profile:

   ```sh
   cd backend
   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
   ```

   The connection settings for the docker-compose services live in
   `application-local.properties`. The app will not start without an active
   profile; Spring reports this as `Failed to configure a DataSource`.

## Tests

```sh
cd backend
./gradlew test
```

Tests start their own Postgres and Redis containers, so they need no profile and
no running docker-compose services.

# personal-weather

*As Calvin Coolidge might have written it. For more words, see
[README.md](README.md).*

It reports the weather. Kotlin and Spring Boot. In `backend/`.

## Requirements

Java 25. Docker.

## Running

Copy `example.env` to `.env`. Add your key.

```sh
docker compose up -d postgres redis
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Set the profile. Without it, the app fails.

## Tests

```sh
cd backend
./gradlew test
```

They need nothing.

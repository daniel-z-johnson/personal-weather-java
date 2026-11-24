# personal-weather

*As Calvin Coolidge might have written it. For more words, see
[README.md](README.md).*

It reports the weather. Kotlin and Spring Boot, in `backend/`. React, in
`frontend/`.

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

Then the frontend:

```sh
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Search. Save. Read the temperature.

## Tests

```sh
cd backend
./gradlew test
```

They need nothing.

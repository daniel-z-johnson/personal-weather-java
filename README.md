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

## API

Errors are returned as [problem details](https://www.rfc-editor.org/rfc/rfc9457) with a `detail` message.
OpenWeather failing gives `502`, and Redis being unreachable gives `503`.

### Locations

| Method & path | Does |
|---|---|
| `GET /api/locations/search?city=..&state=..&country=..` | Searches OpenWeather for places. `state` and `country` are optional. Each result has an `id` that can be saved for an hour (`locations.search-ttl`). |
| `POST /api/locations` with `{"id": "<search result id>"}` | Saves a search result: `201` if new, `200` if it was already saved, `404` if the id expired. |
| `GET /api/locations` | Lists saved locations, oldest first. |
| `DELETE /api/locations/{id}` | Removes a saved location: `204`, or `404` if there is none. |

### Weather

`GET /api/weather` returns the current temperature for every saved location, oldest first:

```json
[{"id":7,"city":"Saint Paul","state":"Minnesota","country":"US",
  "temperatureC":-3.2,"temperatureF":26,"observedAt":"2026-11-20T22:40:00Z"}]
```

- `temperatureC` is rounded to a tenth and `temperatureF` to a whole degree, both converted from
  OpenWeather's unrounded reading. Halves round away from zero.
- `observedAt` is when OpenWeather took the reading.
- Weather is cached per location for 15 minutes (`weather.cache-ttl`).
- If OpenWeather fails for one location, that location is still listed, with `null` weather fields;
  the others are unaffected. Failures aren't cached, so the next request tries again.

## Tests

```sh
cd backend
./gradlew test
```

Tests start their own Postgres and Redis containers, so they need no profile and
no running docker-compose services.

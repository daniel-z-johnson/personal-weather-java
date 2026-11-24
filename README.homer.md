# The Weatheriad

*A song of personal-weather, in the manner of Homer. For plain instructions,
see [README.md](README.md).*

Sing to me, Muse, of the app of many turnings, which reads the moods of the
heavens so that no sailor need again be driven ten years across the wine-dark
sea. Its backend was forged in Kotlin and Spring Boot, as Hephaestus forges
bronze, and it lies in the hall called `backend/`. Its face, which mortals look
upon, was woven in React and TypeScript and Tailwind, as Athena weaves, and it
lies in the hall called `frontend/`.

## Of what the hero must carry

Before the ships are drawn down to the shining water, the builder must carry
two things in the hollow hold:

- **Java 25,** the twenty-fifth of its line, without which no keel will move.
- **Docker,** the many-chambered, in whose holds both the local services and the
  trials of the tests are kept. For the tests summon their own containers
  through Testcontainers, as Zeus summons the clouds.

## Of the launching of the ship

**First**, the builder takes `example.env` and copies it to `.env`, and writes
the OpenWeather key inside it. This key the builder guards as Penelope guarded
her weaving, for `.env` is gitignored, and no suitor will ever find it in the
history.

**Then**, the builder wakes the two faithful servants of the house, Postgres the
keeper of records and Redis the swift of memory:

```sh
docker compose up -d postgres redis
```

**Last**, the builder goes into `backend/` and launches the ship under the
`local` profile, as a captain names his course before he sails:

```sh
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The paths to those servants are written in `application-local.properties`. But
the builder who sails with no profile at all will never leave the harbor. And
Spring will not tell him so plainly. Like the Sirens on their meadow, it sings
only `Failed to configure a DataSource`. The wise builder hears this song, sets
a profile, and sails again.

## Of the face the mortals see

While the ship is on the water, the builder goes into `frontend/` and calls up
the face of the app:

```sh
cd frontend
npm install
npm run dev
```

Then let the builder open http://localhost:5173, as one opens the doors of a
hall to guests. There the builder may search out cities by name, keep the ones
that are dear, and see the warmth or the cold of each, in Fahrenheit and in
Celsius. All that the face asks of `/api`, Vite carries to the backend on
`localhost:8080`, as Hermes carries the words of the gods. So the two halls
speak as one house, and no quarrel of CORS arises between them.

## Of the trials

As Odysseus strung the great bow before the suitors, so the builder proves the
work:

```sh
cd backend
./gradlew test
```

The trials ask no profile and need no docker-compose servants. They raise their
own Postgres and Redis from nothing, then return them to nothing when the trial
is done.

CREATE TABLE locations (
                       id SERIAL PRIMARY KEY,
                       City TEXT NOT NULL,
                       State TEXT NOT NULL DEFAULT '',
                       Country TEXT NOT NULL DEFAULT '',
                       Latitude REAL NOT NULL DEFAULT 0.0,
                       Longitude REAL NOT NULL DEFAULT 0.0
);

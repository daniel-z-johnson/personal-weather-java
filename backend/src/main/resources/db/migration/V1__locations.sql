CREATE TABLE locations (
                       id SERIAL PRIMARY KEY,
                       city TEXT NOT NULL,
                       state TEXT NOT NULL DEFAULT '',
                       country TEXT NOT NULL DEFAULT '',
                       latitude REAL NOT NULL DEFAULT 0.0,
                       longitude REAL NOT NULL DEFAULT 0.0
);

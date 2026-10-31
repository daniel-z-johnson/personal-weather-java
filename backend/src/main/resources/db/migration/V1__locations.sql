CREATE TABLE locations (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    city      TEXT             NOT NULL,
    state     TEXT             NOT NULL DEFAULT '',
    country   TEXT             NOT NULL DEFAULT '',
    latitude  DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    CONSTRAINT locations_lat_lon_unique UNIQUE (latitude, longitude)
);

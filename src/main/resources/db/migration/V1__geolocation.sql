CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    city TEXT NOT NULL,
    state TEXT NOT NULL,
    country TEXT NOT NULL,
    latitude TEXT  NOT NULL DEFAULT '0.0',
    longitude TEXT NOT NULL DEFAULT '0.0'
);
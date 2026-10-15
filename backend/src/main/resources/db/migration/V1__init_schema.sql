CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE stops (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location geography(Point, 4326) NOT NULL
);

CREATE INDEX idx_stops_location ON stops USING GIST (location);

CREATE TABLE routes (
    id VARCHAR(64) PRIMARY KEY,
    short_name VARCHAR(32) NOT NULL,
    long_name VARCHAR(255) NOT NULL,
    mode VARCHAR(16) NOT NULL
);

CREATE TABLE trips (
    id VARCHAR(64) PRIMARY KEY,
    route_id VARCHAR(64) NOT NULL REFERENCES routes (id),
    headsign VARCHAR(255)
);

CREATE TABLE stop_times (
    trip_id VARCHAR(64) NOT NULL REFERENCES trips (id),
    stop_sequence INT NOT NULL,
    stop_id VARCHAR(64) NOT NULL REFERENCES stops (id),
    arrival TIME NOT NULL,
    departure TIME NOT NULL,
    PRIMARY KEY (trip_id, stop_sequence)
);

CREATE INDEX idx_stop_times_stop_id ON stop_times (stop_id);

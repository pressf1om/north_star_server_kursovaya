CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(200) NOT NULL,
    username VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(120) NOT NULL
);

CREATE INDEX idx_users_username ON users (username);

CREATE TABLE cars (
    id SERIAL PRIMARY KEY,
    car_number VARCHAR(120) NOT NULL UNIQUE,
    model VARCHAR(120) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(120),
    car_driver_id INTEGER
);

CREATE INDEX idx_cars_car_number ON cars (car_number);

CREATE TABLE applications (
    id SERIAL PRIMARY KEY,
    coord_start VARCHAR(120) NOT NULL,
    coord_end VARCHAR(120) NOT NULL,
    date_of_start VARCHAR(120) NOT NULL,
    date_of_end VARCHAR(120),
    status VARCHAR(120),
    weight VARCHAR(120),
    car_now VARCHAR(120)
);

CREATE TABLE completed_applications (
    id SERIAL PRIMARY KEY,
    coord_start VARCHAR(120) NOT NULL,
    coord_end VARCHAR(120) NOT NULL,
    date_of_start VARCHAR(120) NOT NULL,
    date_of_end VARCHAR(120),
    status VARCHAR(120),
    weight VARCHAR(120),
    car_now VARCHAR(120)
);

CREATE TABLE routes (
    id SERIAL PRIMARY KEY,
    city_start VARCHAR(120) NOT NULL,
    city_end VARCHAR(120) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    platon_km INTEGER,
    autodor_km INTEGER,
    oll_km INTEGER,
    cities_on_route JSONB
);

CREATE TABLE settings_for_routes (
    id SERIAL PRIMARY KEY,
    driver_salary DOUBLE PRECISION,
    platon_cost DOUBLE PRECISION
);

CREATE TABLE temp_base_for_analytics (
    id SERIAL PRIMARY KEY,
    number_of_road INTEGER,
    date_start VARCHAR(120),
    date_end VARCHAR(120),
    route_id INTEGER,
    cost INTEGER,
    fuel_cost INTEGER
);

CREATE TABLE driver_messages (
    id SERIAL PRIMARY KEY,
    message VARCHAR(5000) NOT NULL,
    car_number VARCHAR(20) NOT NULL
);

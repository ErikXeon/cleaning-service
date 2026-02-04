CREATE TABLE login_attempt (
                               id BIGSERIAL PRIMARY KEY,
                               email VARCHAR(255) NOT NULL,
                               attempts INT NOT NULL,
                               last_attempt TIMESTAMP NOT NULL
);

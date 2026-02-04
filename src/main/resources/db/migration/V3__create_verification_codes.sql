CREATE TABLE verification_codes (
                                    id BIGSERIAL PRIMARY KEY,

                                    user_id BIGINT NOT NULL,

                                    expires_at TIMESTAMP NOT NULL,

                                    attempts INTEGER NOT NULL,

                                    used BOOLEAN NOT NULL,

                                    CONSTRAINT fk_verification_codes_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES users (id)
                                            ON DELETE CASCADE
);

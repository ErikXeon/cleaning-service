CREATE TABLE cleaning_services (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL,
                                   price NUMERIC(10,2) NOT NULL
);

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        client_id BIGINT NOT NULL,
                        cleaning_staff_id BIGINT,
                        manager_id BIGINT,
                        date_time TIMESTAMP,
                        status VARCHAR(50),
                        total_price NUMERIC(10,2),
                        notes TEXT,
                        address TEXT,
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        CONSTRAINT fk_order_client FOREIGN KEY (client_id) REFERENCES users(id),
                        CONSTRAINT fk_order_cleaning_staff FOREIGN KEY (cleaning_staff_id) REFERENCES users(id),
                        CONSTRAINT fk_order_manager FOREIGN KEY (manager_id) REFERENCES users(id)
);


CREATE TABLE order_services (
                                order_id BIGINT NOT NULL,
                                service_id BIGINT NOT NULL,
                                CONSTRAINT fk_order_service_order FOREIGN KEY (order_id) REFERENCES orders(id),
                                CONSTRAINT fk_order_service_service FOREIGN KEY (service_id) REFERENCES cleaning_services(id)
);

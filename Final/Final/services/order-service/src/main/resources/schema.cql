// src/main/resources/schema.cql
CREATE KEYSPACE IF NOT EXISTS order_db
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE order_db;

CREATE TABLE IF NOT EXISTS orders (
                                      order_id TEXT PRIMARY KEY,
                                      user_id TEXT,
                                      status TEXT,
                                      total_amount DECIMAL,
                                      created_at TIMESTAMP,
                                      updated_at TIMESTAMP
);
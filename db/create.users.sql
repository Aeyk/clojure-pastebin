CREATE TABLE IF NOT EXISTS users (
       id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
       timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
       username TEXT NOT NULL UNIQUE,
       password TEXT NOT NULL);


ALTER TABLE users ADD COLUMN role_id INTEGER NOT NULL REFERENCES users_roles(id);
       
CREATE TABLE IF NOT EXISTS users_roles (
       id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
       role_name TEXT NOT NULL UNIQUE);
       
INSERT INTO users_roles (role_name) VALUES ("admin");
INSERT INTO users_roles (role_name) VALUES ("user");
INSERT INTO users_roles (role_name) VALUES ("anon");

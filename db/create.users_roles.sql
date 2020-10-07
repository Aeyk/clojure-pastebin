CREATE TABLE IF NOT EXISTS users_roles (
       id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
       role_name TEXT NOT NULL UNIQUE);

INSERT INTO users_roles (role_name) VALUES ("admin");
INSERT INTO users_roles (role_name) VALUES ("user");
INSERT INTO users_roles (role_name) VALUES ("anon");

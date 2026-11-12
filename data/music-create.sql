CREATE SCHEMA IF NOT EXISTS study;

CREATE TABLE IF NOT EXISTS study.music (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

INSERT INTO study.music (title) VALUES ('Bohemian Rhapsody');
INSERT INTO study.music (title) VALUES ('Hotel California');
INSERT INTO study.music (title) VALUES ('Stairway to Heaven');
INSERT INTO study.music (title) VALUES ('Imagine');
INSERT INTO study.music (title) VALUES ('Shape of You');
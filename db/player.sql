CREATE SCHEMA notorious
  CREATE TABLE player (
    ID SERIAL PRIMARY KEY,
    name CHARACTER (255),
    server_code CHARACTER (255),
    region CHARACTER (255)
  );

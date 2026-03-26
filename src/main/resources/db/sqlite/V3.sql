ALTER TABLE clan_members ADD COLUMN player_uuid TEXT;

-- Make leader nullable and add leader_uuid (Java step populates leader_uuid then drops leader)
CREATE TABLE clan_list_new (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    uuid        TEXT    NOT NULL UNIQUE,
    name        TEXT    NOT NULL UNIQUE,
    leader      TEXT,
    leader_uuid TEXT,
    max_players INTEGER NOT NULL,
    level       INTEGER NOT NULL
);
INSERT INTO clan_list_new (id, uuid, name, leader, max_players, level)
SELECT id, uuid, name, leader, max_players, level FROM clan_list;
DROP TABLE clan_list;
ALTER TABLE clan_list_new RENAME TO clan_list;
DELETE FROM clan_data WHERE ident='home' AND value='none'

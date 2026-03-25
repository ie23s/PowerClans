ALTER TABLE clan_members ADD COLUMN player_uuid TEXT;

CREATE TABLE clan_list_new (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    uuid        TEXT    NOT NULL UNIQUE,
    name        TEXT    NOT NULL UNIQUE,
    leader_uuid TEXT,
    max_players INTEGER NOT NULL,
    level       INTEGER NOT NULL
);
INSERT INTO clan_list_new (id, uuid, name, max_players, level)
SELECT id, uuid, name, max_players, level FROM clan_list;
DROP TABLE clan_list;
ALTER TABLE clan_list_new RENAME TO clan_list;

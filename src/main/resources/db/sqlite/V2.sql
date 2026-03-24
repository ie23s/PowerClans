-- SQLite does not support DROP/RENAME COLUMN without table recreation.
-- Strategy: create new tables, migrate data, drop old tables, rename.

-- Step 1: New clan_list with id as AI PK and uuid as UNIQUE
CREATE TABLE clan_list_new (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    uuid        TEXT    NOT NULL UNIQUE,
    name        TEXT    NOT NULL UNIQUE,
    leader      TEXT    NOT NULL,
    max_players INTEGER NOT NULL,
    level       INTEGER NOT NULL
);

-- Step 2: Migrate clans, generating a v4 UUID for each row
INSERT INTO clan_list_new (uuid, name, leader, max_players, level)
SELECT lower(hex(randomblob(4))) || '-' ||
       lower(hex(randomblob(2))) || '-4' ||
       substr(lower(hex(randomblob(2))), 2) || '-' ||
       substr('89ab', abs(random()) % 4 + 1, 1) ||
       substr(lower(hex(randomblob(2))), 2) || '-' ||
       lower(hex(randomblob(6))),
       name, leader, maxplayers, level
FROM clan_list;

-- Step 3: Create clan_data as EAV table (FK enforcement requires PRAGMA foreign_keys=ON)
CREATE TABLE clan_data (
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    clan_uuid TEXT    NOT NULL,
    ident     TEXT    NOT NULL,
    value     TEXT    NOT NULL,
    UNIQUE (clan_uuid, ident),
    FOREIGN KEY (clan_uuid) REFERENCES clan_list(uuid) ON DELETE CASCADE
);

-- Step 4: Migrate secondary data as EAV rows, joining on name to get the newly generated uuid
INSERT INTO clan_data (clan_uuid, ident, value)
SELECT n.uuid, 'tag',          old.tag          FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'home',         old.home         FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'pvp',          old.pvp          FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'balance',      old.balance      FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'mob_kills',    old.mobkills     FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'player_kills', old.playerkills  FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'online_time',  old.onlinetime   FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name;

-- Step 5: Create clan_members_new with clan_uuid FK replacing the name-based clan column
CREATE TABLE clan_members_new (
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    clan_uuid TEXT    NOT NULL,
    name      TEXT    NOT NULL,
    isModer   INTEGER NOT NULL,
    UNIQUE (clan_uuid, name),
    FOREIGN KEY (clan_uuid) REFERENCES clan_list(uuid) ON DELETE CASCADE
);

-- Step 6: Migrate members, resolving clan name -> uuid via JOIN
INSERT INTO clan_members_new (clan_uuid, name, isModer)
SELECT n.uuid, cm.name, cm.isModer
FROM clan_members cm
INNER JOIN clan_list_new n ON cm.clan = n.name;

-- Step 7: Swap all tables
DROP TABLE clan_list;
ALTER TABLE clan_list_new RENAME TO clan_list;
DROP TABLE clan_members;
ALTER TABLE clan_members_new RENAME TO clan_members;

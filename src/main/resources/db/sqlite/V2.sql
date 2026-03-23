-- SQLite does not support DROP/RENAME COLUMN without table recreation.
-- Strategy: create new tables, migrate data via name-based JOIN, drop old table.

-- Step 1: New clan_list with uuid as PK
CREATE TABLE clan_list_v2 (
    uuid        TEXT    NOT NULL PRIMARY KEY,
    name        TEXT    NOT NULL UNIQUE,
    leader      TEXT    NOT NULL,
    max_players INTEGER NOT NULL,
    level       INTEGER NOT NULL
);

-- Step 2: Migrate clans, generating a v4 UUID for each row
INSERT INTO clan_list_v2 (uuid, name, leader, max_players, level)
SELECT lower(hex(randomblob(4))) || '-' ||
       lower(hex(randomblob(2))) || '-4' ||
       substr(lower(hex(randomblob(2))), 2) || '-' ||
       substr('89ab', abs(random()) % 4 + 1, 1) ||
       substr(lower(hex(randomblob(2))), 2) || '-' ||
       lower(hex(randomblob(6))),
       name, leader, maxplayers, level
FROM clan_list;

-- Step 3: Create clan_data (no FK — SQLite FK support is off by default anyway)
CREATE TABLE clan_data (
    clan_uuid    TEXT    NOT NULL PRIMARY KEY,
    tag          TEXT    NOT NULL DEFAULT '',
    home         TEXT    NOT NULL DEFAULT 'none',
    pvp          INTEGER NOT NULL DEFAULT 0,
    balance      REAL    NOT NULL DEFAULT 0,
    mob_kills    INTEGER NOT NULL DEFAULT 0,
    player_kills INTEGER NOT NULL DEFAULT 0,
    online_time  INTEGER NOT NULL DEFAULT 0
);

-- Step 4: Migrate secondary data, joining on name to get the newly generated uuid
INSERT INTO clan_data (clan_uuid, tag, home, pvp, balance, mob_kills, player_kills, online_time)
SELECT v2.uuid, old.tag, old.home, old.pvp, old.balance, old.mobkills, old.playerkills, old.onlinetime
FROM clan_list_v2 v2
INNER JOIN clan_list old ON v2.name = old.name;

-- Step 5: Swap tables
DROP TABLE clan_list;
ALTER TABLE clan_list_v2 RENAME TO clan_list;

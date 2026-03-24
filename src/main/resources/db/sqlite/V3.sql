-- SQLite does not support adding FK constraints to existing tables.
-- Strategy: recreate clan_members with clan_uuid instead of clan (name).

-- Step 1: New clan_members with clan_uuid FK
CREATE TABLE clan_members_v3 (
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    clan_uuid TEXT    NOT NULL,
    name      TEXT    NOT NULL,
    isModer   INTEGER NOT NULL,
    UNIQUE (clan_uuid, name),
    FOREIGN KEY (clan_uuid) REFERENCES clan_list(uuid) ON DELETE CASCADE
);

-- Step 2: Migrate data, resolving clan name → uuid via JOIN
INSERT INTO clan_members_v3 (clan_uuid, name, isModer)
SELECT cl.uuid, cm.name, cm.isModer
FROM clan_members cm
INNER JOIN clan_list cl ON cm.clan = cl.name;

-- Step 3: Swap tables
DROP TABLE clan_members;
ALTER TABLE clan_members_v3 RENAME TO clan_members;

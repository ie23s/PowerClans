-- Step 1: Create clan_list_new with correct column order and native UUID type
CREATE TABLE clan_list_new (
    id          INT          NOT NULL AUTO_INCREMENT,
    uuid        UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    leader      VARCHAR(255) NOT NULL,
    max_players INT          NOT NULL,
    level       INT          NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_clan_name (name),
    UNIQUE KEY uq_clan_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Migrate clans, generating a v4 UUID for each row
INSERT INTO clan_list_new (uuid, name, leader, max_players, level)
SELECT UUID(), name, leader, maxplayers, level
FROM clan_list;

-- Step 3: Create clan_data as EAV table with FK to clan_list_new
CREATE TABLE clan_data (
    id        INT          NOT NULL AUTO_INCREMENT,
    clan_uuid UUID         NOT NULL,
    ident     VARCHAR(64)  NOT NULL,
    value     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_clan_data (clan_uuid, ident),
    CONSTRAINT fk_clan_data FOREIGN KEY (clan_uuid) REFERENCES clan_list_new(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 4: Migrate secondary data as EAV rows, joining on name to get the newly generated uuid
INSERT INTO clan_data (clan_uuid, ident, value)
SELECT n.uuid, 'tag',          old.tag         FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'home',         old.home        FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'pvp',          old.pvp         FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'balance',      old.balance     FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'mob_kills',    old.mobkills    FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'player_kills', old.playerkills FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name
UNION ALL
SELECT n.uuid, 'online_time',  old.onlinetime  FROM clan_list_new n INNER JOIN clan_list old ON n.name = old.name;

-- Step 5: Create clan_members_new with clan_uuid replacing the name-based clan column
CREATE TABLE clan_members_new (
    id        INT          NOT NULL AUTO_INCREMENT,
    clan_uuid UUID         NOT NULL,
    name      VARCHAR(255) NOT NULL,
    isModer   BOOLEAN      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_clan_member (clan_uuid, name),
    CONSTRAINT fk_clan_members_clan FOREIGN KEY (clan_uuid) REFERENCES clan_list_new(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 6: Migrate members, resolving clan name -> uuid via JOIN
INSERT INTO clan_members_new (clan_uuid, name, isModer)
SELECT n.uuid, cm.name, cm.isModer
FROM clan_members cm
INNER JOIN clan_list_new n ON cm.clan = n.name;

-- Step 7: Swap all tables (MySQL automatically updates FK references on rename)
DROP TABLE clan_list;
RENAME TABLE clan_list_new TO clan_list;
DROP TABLE clan_members;
RENAME TABLE clan_members_new TO clan_members;

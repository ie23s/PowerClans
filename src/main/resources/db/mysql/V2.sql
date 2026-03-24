-- Step 1: Create clan_list_v2 with correct column order and CHAR(36) uuid
CREATE TABLE clan_list_v2 (
    id          INT          NOT NULL AUTO_INCREMENT,
    uuid        CHAR(36)     NOT NULL,
    name        VARCHAR(255) NOT NULL,
    leader      VARCHAR(255) NOT NULL,
    max_players INT          NOT NULL,
    level       INT          NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_clan_name (name),
    UNIQUE KEY uq_clan_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Migrate clans, generating a v4 UUID for each row
INSERT INTO clan_list_v2 (uuid, name, leader, max_players, level)
SELECT UUID(), name, leader, maxplayers, level
FROM clan_list;

-- Step 3: Create clan_data with FK to clan_list_v2
CREATE TABLE IF NOT EXISTS clan_data (
    clan_uuid    CHAR(36)     NOT NULL,
    tag          VARCHAR(255) NOT NULL DEFAULT '',
    home         VARCHAR(255) NOT NULL DEFAULT 'none',
    pvp          TINYINT(1)   NOT NULL DEFAULT 0,
    balance      DOUBLE       NOT NULL DEFAULT 0,
    mob_kills    INT          NOT NULL DEFAULT 0,
    player_kills INT          NOT NULL DEFAULT 0,
    online_time  INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (clan_uuid),
    CONSTRAINT fk_clan_data FOREIGN KEY (clan_uuid) REFERENCES clan_list_v2(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 4: Migrate secondary data, joining on name to get the newly generated uuid
INSERT INTO clan_data (clan_uuid, tag, home, pvp, balance, mob_kills, player_kills, online_time)
SELECT v2.uuid, old.tag, old.home, old.pvp, old.balance, old.mobkills, old.playerkills, old.onlinetime
FROM clan_list_v2 v2
INNER JOIN clan_list old ON v2.name = old.name;

-- Step 5: Swap tables (MySQL automatically updates FK references on rename)
DROP TABLE clan_list;
RENAME TABLE clan_list_v2 TO clan_list;

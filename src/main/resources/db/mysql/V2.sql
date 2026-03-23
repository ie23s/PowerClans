-- Step 1: Add uuid to clan_list and backfill
ALTER TABLE clan_list ADD COLUMN uuid VARCHAR(36) NOT NULL DEFAULT '';
UPDATE clan_list SET uuid = UUID();
ALTER TABLE clan_list DROP PRIMARY KEY;
ALTER TABLE clan_list DROP COLUMN id;
ALTER TABLE clan_list ADD PRIMARY KEY (uuid);

-- Step 2: Create clan_data
CREATE TABLE IF NOT EXISTS clan_data (
    clan_uuid    VARCHAR(36)  NOT NULL,
    tag          VARCHAR(255) NOT NULL DEFAULT '',
    home         VARCHAR(255) NOT NULL DEFAULT 'none',
    pvp          TINYINT(1)   NOT NULL DEFAULT 0,
    balance      DOUBLE       NOT NULL DEFAULT 0,
    mob_kills    INT          NOT NULL DEFAULT 0,
    player_kills INT          NOT NULL DEFAULT 0,
    online_time  INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (clan_uuid),
    CONSTRAINT fk_clan_data FOREIGN KEY (clan_uuid) REFERENCES clan_list(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 3: Migrate secondary data
INSERT INTO clan_data (clan_uuid, tag, home, pvp, balance, mob_kills, player_kills, online_time)
SELECT uuid, tag, home, pvp, balance, mobkills, playerkills, onlinetime
FROM clan_list;

-- Step 4: Rename and drop columns
ALTER TABLE clan_list RENAME COLUMN maxplayers TO max_players;
ALTER TABLE clan_list DROP COLUMN tag;
ALTER TABLE clan_list DROP COLUMN home;
ALTER TABLE clan_list DROP COLUMN pvp;
ALTER TABLE clan_list DROP COLUMN balance;
ALTER TABLE clan_list DROP COLUMN mobkills;
ALTER TABLE clan_list DROP COLUMN playerkills;
ALTER TABLE clan_list DROP COLUMN onlinetime;

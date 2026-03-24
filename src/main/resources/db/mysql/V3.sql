-- Step 1: Create clan_members_v3 with correct column order and CHAR(36) uuid
CREATE TABLE clan_members_v3 (
    id        INT          NOT NULL AUTO_INCREMENT,
    clan_uuid CHAR(36)     NOT NULL,
    name      VARCHAR(255) NOT NULL,
    isModer   TINYINT(1)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_clan_member (clan_uuid, name),
    CONSTRAINT fk_clan_members_clan FOREIGN KEY (clan_uuid) REFERENCES clan_list(uuid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Step 2: Migrate data, resolving clan name -> uuid via JOIN
INSERT INTO clan_members_v3 (clan_uuid, name, isModer)
SELECT cl.uuid, cm.name, cm.isModer
FROM clan_members cm
INNER JOIN clan_list cl ON cm.clan = cl.name;

-- Step 3: Swap tables
DROP TABLE clan_members;
RENAME TABLE clan_members_v3 TO clan_members;

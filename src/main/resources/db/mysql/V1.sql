CREATE TABLE IF NOT EXISTS `clan_list` (
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(255) NOT NULL,
    `tag`         VARCHAR(255) NOT NULL,
    `leader`      VARCHAR(255) NOT NULL,
    `home`        VARCHAR(255) NOT NULL,
    `maxplayers`  INT          NOT NULL,
    `pvp`         TINYINT(1)   NOT NULL,
    `balance`     DOUBLE       NOT NULL,
    `mobkills`    INT          NOT NULL,
    `playerkills` INT          NOT NULL,
    `onlinetime`  INT          NOT NULL,
    `level`       INT          NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_clan_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `clan_members` (
    `id`      INT          NOT NULL AUTO_INCREMENT,
    `clan`    VARCHAR(255) NOT NULL,
    `name`    VARCHAR(255) NOT NULL,
    `isModer` TINYINT(1)   NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_clan_member` (`clan`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

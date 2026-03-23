CREATE TABLE IF NOT EXISTS `clan_list` (
    `id`          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    `name`        TEXT    NOT NULL UNIQUE,
    `tag`         TEXT    NOT NULL,
    `leader`      TEXT    NOT NULL,
    `home`        TEXT    NOT NULL,
    `maxplayers`  INTEGER NOT NULL,
    `pvp`         INTEGER NOT NULL,
    `balance`     REAL    NOT NULL,
    `mobkills`    INTEGER NOT NULL,
    `playerkills` INTEGER NOT NULL,
    `onlinetime`  INTEGER NOT NULL,
    `level`       INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `clan_members` (
    `id`      INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    `clan`    TEXT    NOT NULL,
    `name`    TEXT    NOT NULL,
    `isModer` INTEGER NOT NULL,
    UNIQUE (`clan`, `name`)
);

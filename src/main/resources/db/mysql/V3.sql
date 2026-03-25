ALTER TABLE clan_members ADD COLUMN player_uuid UUID AFTER name;
ALTER TABLE clan_list ADD COLUMN leader_uuid UUID AFTER leader;

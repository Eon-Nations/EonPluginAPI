
CREATE TABLE IF NOT EXISTS vaults (
    vault_id INT PRIMARY KEY AUTO_INCREMENT,
    copper INT DEFAULT 0 NOT NULL,
    CHECK (copper >= 0),
    iron INT DEFAULT 0 NOT NULL,
    CHECK (iron >= 0),
    gold INT DEFAULT 0 NOT NULL,
    CHECK (gold >= 0),
    diamonds INT DEFAULT 0 NOT NULL,
    CHECK (diamonds >= 0),
    emeralds INT DEFAULT 0 NOT NULL,
    CHECK (emeralds >= 0)
);

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_vault(OUT new_vault_id INT)
BEGIN
    INSERT INTO vaults () VALUES ();
    SELECT LAST_INSERT_ID()
        INTO new_vault_id;
END //

CREATE PROCEDURE IF NOT EXISTS remove_vault(IN in_vault_id INT)
BEGIN
    DELETE FROM vaults WHERE vault_id=in_vault_id;
END //

CREATE PROCEDURE IF NOT EXISTS get_vault(IN in_vault_id INT)
READS SQL DATA
BEGIN
    SELECT
        vault_id,
        copper,
        iron,
        gold,
        diamonds,
        emeralds
    FROM vaults
    WHERE vault_id = in_vault_id;
END //

DELIMITER ;

CREATE TABLE IF NOT EXISTS spawns (
    spawn_id INT PRIMARY KEY AUTO_INCREMENT,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    yaw FLOAT NOT NULL,
    pitch FLOAT NOT NULL
);

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_spawn(IN in_x INT, IN in_y INT, IN in_z INT, IN in_yaw FLOAT, IN in_pitch FLOAT, OUT new_spawn_id INT)
MODIFIES SQL DATA
BEGIN
    INSERT INTO spawns(x, y, z, yaw, pitch)
        VALUES (in_x, in_y, in_z, in_yaw, in_pitch);
    SELECT LAST_INSERT_ID()
        INTO new_spawn_id;
END//

CREATE PROCEDURE IF NOT EXISTS remove_spawn(IN in_spawn_id INT)
MODIFIES SQL DATA
BEGIN
    DELETE FROM spawns WHERE spawn_id=in_spawn_id;
END//

DELIMITER ;


CREATE TABLE IF NOT EXISTS players (
    player_uuid BINARY(16) PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    donor_rank VARCHAR(20) NOT NULL DEFAULT 'Player',
    level INT NOT NULL DEFAULT 1,
    last_online TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vault_id INT NOT NULL,
    FOREIGN KEY (vault_id) REFERENCES vaults(vault_id)
);

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_new_player(IN in_uuid VARCHAR(36), IN in_username VARCHAR(20))
BEGIN
    DECLARE uuid_not_valid CONDITION FOR SQLSTATE '99001';
    DECLARE username_taken CONDITION FOR SQLSTATE '99002';
    DECLARE new_vault_id INT;
    DECLARE EXIT HANDLER FOR 1062
        SIGNAL username_taken SET MESSAGE_TEXT = 'Username already taken';
    IF IS_UUID(in_uuid) THEN
        CALL create_vault(new_vault_id);
        INSERT INTO players(player_uuid, username, vault_id)
             VALUES (UUID_TO_BIN(in_uuid), in_username, new_vault_id);
    ELSE
        SIGNAL uuid_not_valid SET MESSAGE_TEXT = 'UUID is not valid for player creation';
    END IF;
END //

CREATE PROCEDURE IF NOT EXISTS get_player_uuid(IN in_username VARCHAR(20))
BEGIN
    SELECT
        BIN_TO_UUID(player_uuid)
    FROM players
    WHERE username = in_username;
END //

CREATE PROCEDURE IF NOT EXISTS level_up_player(IN in_uuid VARCHAR(36), OUT out_level INT)
BEGIN
    DECLARE new_level INT DEFAULT 1;
    DECLARE p_uuid BINARY(16) DEFAULT UUID_TO_BIN(in_uuid);

    SELECT
        level
        INTO new_level
    FROM players
    WHERE player_uuid = p_uuid;

    SET new_level = new_level + 1;
    UPDATE players
    SET level=new_level
    WHERE player_uuid = p_uuid;

    SET out_level = new_level;
END //

CREATE PROCEDURE IF NOT EXISTS player_vault(IN in_uuid VARCHAR(36))
BEGIN
    DECLARE p_uuid BINARY(16) DEFAULT UUID_TO_BIN(in_uuid);

    SELECT
        copper,
        iron,
        gold,
        diamonds,
        emeralds,
        vaults.vault_id
    FROM vaults
    INNER JOIN players p ON vaults.vault_id = p.vault_id
    WHERE p.player_uuid = p_uuid;
END //

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS retrieve_player(IN in_uuid VARCHAR(36))
BEGIN
     DECLARE p_uuid BINARY(16) DEFAULT UUID_TO_BIN(in_uuid);

    SELECT
        donor_rank,
        level,
        last_online,
        town_name,
        players.vault_id as vault_id,
        copper,
        iron,
        gold,
        diamonds,
        emeralds
    FROM players
    LEFT JOIN player_towns pt on players.player_uuid = pt.player_uuid
    INNER JOIN vaults v on players.vault_id = v.vault_id
    WHERE players.player_uuid = p_uuid;
END //

DELIMITER ;

CREATE TABLE IF NOT EXISTS towns (
    name VARCHAR(20) PRIMARY KEY,
    owner BINARY(16) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    vault_id INT NOT NULL,
    spawn_id INT NOT NULL,
    FOREIGN KEY (vault_id) REFERENCES vaults(vault_id),
    FOREIGN KEY (spawn_id) REFERENCES spawns(spawn_id)
);

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_full_town(IN town_name VARCHAR(20), IN owner_uuid VARCHAR(36), IN x INT, IN y INT, IN z INT, IN yaw FLOAT, IN pitch FLOAT)
BEGIN
    DECLARE new_vault INT DEFAULT -1;
    DECLARE new_spawn INT DEFAULT -1;
    CALL create_vault(new_vault);
    CALL create_spawn(x, y, z, yaw, pitch, new_spawn);
    INSERT INTO towns(name, owner, vault_id, spawn_id)
       VALUES (town_name, UUID_TO_BIN(owner_uuid), new_vault, new_spawn);
    INSERT INTO player_towns(town_name, player_uuid)
       VALUES (town_name, UUID_TO_BIN(owner_uuid));
    SELECT
        vault_id,
        copper,
        iron,
        gold,
        diamonds,
        emeralds
    FROM vaults
    WHERE vault_id = new_vault;
END //

CREATE PROCEDURE IF NOT EXISTS retrieve_town(IN town_name VARCHAR(20))
BEGIN
     SELECT
         BIN_TO_UUID(owner) as owner,
         level,
         v.vault_id as vault_id,
         s.x as x,
         s.y as y,
         s.z as z,
         s.pitch as pitch,
         s.yaw as yaw,
         v.copper as copper,
         v.iron as iron,
         v.gold as gold,
         v.diamonds as diamonds,
         v.emeralds as emeralds
     FROM towns
     JOIN spawns s ON s.spawn_id = towns.spawn_id
     JOIN vaults v ON towns.vault_id = v.vault_id
     WHERE name = town_name;
END //

CREATE PROCEDURE IF NOT EXISTS change_town_spawn(IN town_name VARCHAR(20), IN in_x INT, IN in_y INT, IN in_z INT, IN in_yaw FLOAT, IN in_pitch FLOAT)
BEGIN
    UPDATE spawns
    SET x=in_x, y=in_y, z=in_z, yaw=in_yaw, pitch=in_pitch
    WHERE spawn_id=(
        SELECT spawn_id
        FROM towns
        WHERE name=town_name
        );
END //

CREATE PROCEDURE IF NOT EXISTS remove_town(IN in_town_name VARCHAR(20))
BEGIN
   DELETE FROM towns WHERE name = in_town_name;
   DELETE FROM player_towns WHERE town_name = in_town_name;
END //

CREATE PROCEDURE IF NOT EXISTS add_player_to_town(IN in_owner_uuid VARCHAR(36), IN in_town_name VARCHAR(20))
BEGIN
    INSERT INTO player_towns (town_name, player_uuid)
        VALUES (in_town_name, UUID_TO_BIN(in_owner_uuid));
END //

CREATE PROCEDURE IF NOT EXISTS remove_player_from_town(IN in_owner_uuid VARCHAR(36))
BEGIN
    DELETE FROM player_towns WHERE player_uuid = UUID_TO_BIN(in_owner_uuid);
END //

CREATE PROCEDURE IF NOT EXISTS level_up_town(IN town_name VARCHAR(20), OUT new_level INT)
BEGIN
    UPDATE towns
    SET level = level + 1
    WHERE name = town_name;
    SELECT
        level INTO new_level
    FROM towns
    WHERE name = town_name;
END //

DELIMITER ;

CREATE TABLE IF NOT EXISTS player_towns (
    town_name VARCHAR(20),
    player_uuid BINARY(16),
    PRIMARY KEY (town_name, player_uuid),
    FOREIGN KEY (town_name) REFERENCES towns(name),
    FOREIGN KEY (player_uuid) REFERENCES players(player_uuid)
);

CREATE TABLE IF NOT EXISTS nations (
    name VARCHAR(20) PRIMARY KEY,
    owner_town VARCHAR(20) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    vault_id INT NOT NULL,
    FOREIGN KEY (vault_id) REFERENCES vaults(vault_id)
);

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_nation(IN in_name VARCHAR(20), IN in_town VARCHAR(20))
BEGIN
    DECLARE new_vault_id INT DEFAULT -1;
    CALL create_vault(new_vault_id);
    INSERT INTO nations(name, owner_town, vault_id)
        VALUES (in_name, in_town, new_vault_id);
    CALL add_town_to_nation(in_town, in_name);
END //

CREATE PROCEDURE IF NOT EXISTS remove_nation(IN in_name VARCHAR(20))
BEGIN
    DELETE FROM nation_members WHERE nation_name = in_name;
    DELETE FROM nations WHERE name = in_name;
END //

CREATE PROCEDURE IF NOT EXISTS level_up_nation(IN in_name VARCHAR(20), OUT out_level INT)
BEGIN
    UPDATE nations
    SET level = level + 1
    WHERE nations.name = in_name;
    SELECT
        level INTO out_level
    FROM nations
    WHERE name = in_name;
END //

CREATE PROCEDURE IF NOT EXISTS retrieve_nation(IN nation_name VARCHAR(20))
BEGIN
    SELECT
        owner_town,
        v.vault_id,
        level,
        copper,
        gold,
        diamonds,
        emeralds,
        iron
    FROM nations
    JOIN vaults v ON nations.vault_id = v.vault_id
    WHERE name = nation_name;
END //

CREATE PROCEDURE IF NOT EXISTS add_town_to_nation(IN in_town_name VARCHAR(20), IN in_nation_name VARCHAR(20))
BEGIN
     DECLARE town_already_in_nation CONDITION FOR SQLSTATE '99001';
     DECLARE existing_nation VARCHAR(20) DEFAULT '';
     SELECT
         nation_name INTO existing_nation
     FROM nation_members
     WHERE town_name = in_town_name;
     IF existing_nation = '' THEN
         INSERT INTO nation_members (town_name, nation_name)
             VALUES (in_town_name, in_nation_name);
     ELSE
         SIGNAL town_already_in_nation SET MESSAGE_TEXT = 'Town is already in a nation';
     END IF;
END //

CREATE PROCEDURE IF NOT EXISTS remove_town_from_nation(IN in_town_name VARCHAR(20))
BEGIN
    DELETE FROM nation_members WHERE town_name = in_town_name;
END //

DELIMITER ;

CREATE TABLE IF NOT EXISTS nation_members (
    nation_name VARCHAR(20),
    town_name VARCHAR(20),
    PRIMARY KEY (town_name, nation_name),
    FOREIGN KEY (nation_name) REFERENCES nations(name),
    FOREIGN KEY (town_name) REFERENCES towns(name)
);

CREATE TABLE IF NOT EXISTS votes (
    player_uuid BINARY(16),
    vote_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    website VARCHAR(100) NOT NULL,
    PRIMARY KEY (player_uuid, vote_date),
    FOREIGN KEY (player_uuid) REFERENCES players(player_uuid)
);

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS new_vote(IN in_uuid VARCHAR(36), IN in_website VARCHAR(100))
BEGIN
    INSERT INTO votes (player_uuid, website)
        VALUES (in_uuid, in_website);
END //

CREATE TABLE IF NOT EXISTS nodes (
    node_id INT PRIMARY KEY AUTO_INCREMENT,
    owner_town VARCHAR(20) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    resource ENUM('copper', 'iron', 'gold', 'diamonds', 'emeralds') NOT NULL,
    output_rate INT NOT NULL,
    FOREIGN KEY (owner_town) REFERENCES towns(name)
);

CREATE PROCEDURE IF NOT EXISTS create_node(IN in_owner VARCHAR(20), IN in_x INT, IN in_y INT, IN in_z INT, IN in_resource VARCHAR(20), IN in_output INT)
BEGIN
    INSERT INTO nodes (owner_town, x, y, z, resource, output_rate)
        VALUES (in_owner, in_x, in_y, in_z, in_resource, in_output);
END //

CREATE TABLE IF NOT EXISTS wars (
    declaring_entity VARCHAR(20) NOT NULL,
    defending_entity VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimatum_deal TEXT NOT NULL,
    PRIMARY KEY (declaring_entity, defending_entity)
);
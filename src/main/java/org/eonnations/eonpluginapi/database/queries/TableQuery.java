package org.eonnations.eonpluginapi.database.queries;

public class TableQuery {

    private TableQuery() { }
    public static final String CREATE_VAULTS_TABLE = """
            CREATE TABLE IF NOT EXISTS Vaults (
            VaultID INT AUTO_INCREMENT PRIMARY KEY,
            Coins INT DEFAULT 0,
            Iron INT DEFAULT 0,
            Gold INT DEFAULT 0,
            Diamonds INT DEFAULT 0,
            Emeralds INT DEFAULT 0);
            """;

    public static final String CREATE_SPAWN_TABLE = """
            CREATE TABLE IF NOT EXISTS Spawns (
            SpawnID INT NOT NULL AUTO_INCREMENT,
            X INT NOT NULL,
            Y INT NOT NULL,
            Z INT NOT NULL,
            Yaw FLOAT NOT NULL,
            Pitch FLOAT NOT NULL,
            PRIMARY KEY (SpawnID));
            """;

    public static final String CREATE_PLAYERS_TABLE = """
            CREATE TABLE IF NOT EXISTS Players (
            UUID VARCHAR(36) PRIMARY KEY,
            Username VARCHAR(255),
            Level VARCHAR(50),
            DonorRank VARCHAR(50),
            VaultID INT,
            FOREIGN KEY (VaultID) REFERENCES Vaults(VaultID));
            """;

    public static final String CREATE_PLAYER_TOWN_TABLE = """
            CREATE TABLE IF NOT EXISTS PlayerTowns (
            UUID VARCHAR(36),
            Town VARCHAR(255),
            PRIMARY KEY (UUID, Town),
            FOREIGN KEY (UUID) REFERENCES Players(UUID),
            FOREIGN KEY (Town) REFERENCES Towns(Name));
          """;
    public static final String CREATE_TOWNS_TABLE = """
            CREATE TABLE IF NOT EXISTS Towns (
            Name VARCHAR(255) PRIMARY KEY,
            Owner VARCHAR(36) NOT NULL,
            VaultID INT NOT NULL,
            Spawn INT NOT NULL,
            FOREIGN KEY (VaultID) REFERENCES Vaults(VaultID),
            FOREIGN KEY (Spawn) REFERENCES Spawns(SpawnID));
            """;

    public static final String CREATE_NATIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS Nations (
            Name VARCHAR(255) PRIMARY KEY,
            Owner VARCHAR(255) NOT NULL,
            Level INT DEFAULT 1,
            VaultID INT NOT NULL,
            FOREIGN KEY (VaultID) REFERENCES Vaults(VaultID));
            """;

    public static final String CREATE_TOWN_MEMBER_TABLE = """
            CREATE TABLE IF NOT EXISTS TownMembers (
            Nation VARCHAR(255),
            Town VARCHAR(255),
            PRIMARY KEY (Nation, Town),
            FOREIGN KEY (Nation) REFERENCES Nations(Name),
            FOREIGN KEY (Town) REFERENCES Towns(Name));
            """;

    public static final String CREATE_VOTES_TABLE = """
            CREATE TABLE IF NOT EXISTS Votes (
            UUID VARCHAR(36),
            Date DATETIME,
            Website VARCHAR(255) NOT NULL,
            PRIMARY KEY (UUID, Date));
            """;

    public static final String CREATE_NODES_TABLE = """
            CREATE TABLE IF NOT EXISTS Nodes (
            NodeID INT AUTO_INCREMENT PRIMARY KEY,
            OwnerUUID VARCHAR(36) NOT NULL,
            x INT,
            y INT,
            z INT,
            Resource VARCHAR(50),
            Output INT,
            FOREIGN KEY (OwnerUUID) REFERENCES Players(UUID));
            """;

    public static final String CREATE_WARS_TABLE = """
            CREATE TABLE IF NOT EXISTS Wars (
            DeclaringEntity VARCHAR(255),
            DefendingEntity VARCHAR(255),
            StartDate DATETIME,
            UltimatumDeal TEXT,
            PRIMARY KEY (DeclaringEntity, DefendingEntity));
            """;
}

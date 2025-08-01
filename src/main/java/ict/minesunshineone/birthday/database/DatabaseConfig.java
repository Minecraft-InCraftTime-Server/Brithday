package ict.minesunshineone.birthday.database;

import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSSL;
    private final String tablePrefix;
    private final int maxConnections;
    private final int connectionTimeout;

    public DatabaseConfig(FileConfiguration config) {
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "minecraft");
        this.username = config.getString("database.username", "root");
        this.password = config.getString("database.password", "");
        this.useSSL = config.getBoolean("database.useSSL", false);
        this.tablePrefix = config.getString("database.table-prefix", "birthday_");
        this.maxConnections = config.getInt("database.max-connections", 10);
        this.connectionTimeout = config.getInt("database.connection-timeout", 5000);
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isUseSSL() { return useSSL; }
    public String getTablePrefix() { return tablePrefix; }
    public int getMaxConnections() { return maxConnections; }
    public int getConnectionTimeout() { return connectionTimeout; }

    public String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&allowReconnect=true&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8",
                host, port, database, useSSL);
    }
}

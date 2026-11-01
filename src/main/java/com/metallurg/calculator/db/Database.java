package com.metallurg.calculator.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Подключение к локальной SQLite БД и инициализация схемы.
 */
public final class Database {

    private static final Database INSTANCE = new Database();
    private final Path dbPath;
    private Connection connection;

    private Database() {
        Path dataDir = Path.of(System.getProperty("user.dir"), "data");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать каталог data", e);
        }
        this.dbPath = dataDir.resolve("metallurg.db");
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            runSchema();
        }
    }

    private void runSchema() throws IOException, SQLException {
        try (InputStream in = Database.class.getResourceAsStream("/sql/schema.sql")) {
            if (in == null) {
                throw new IOException("Не найден /sql/schema.sql");
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement st = connection.createStatement()) {
                for (String part : sql.split(";")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        st.execute(trimmed);
                    }
                }
            }
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("БД не инициализирована");
        }
        return connection;
    }

    public Path getDbPath() {
        return dbPath;
    }
}

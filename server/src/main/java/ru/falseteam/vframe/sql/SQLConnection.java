package ru.vladislavsumin.myhome.server.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.config.ConfigLoader;
import ru.falseteam.vframe.config.LoadFromConfig;

import java.sql.*;

/**
 * Create single connection to database.
 * Send all requests to database.
 *
 * @author Evgeny Rudzyansky
 * @author Sumin Vladislav
 * @version 1.0
 */
public class SQLConnection {
    private static final Logger log = LogManager.getLogger();

    @LoadFromConfig(filename = "database", defaultValue = "jdbc:mysql://localhost:3306?useSSL=false?autoReconnect=true")
    private static String url;
    @LoadFromConfig(filename = "database", defaultValue = "MyHome")
    private static String username;
    @LoadFromConfig(filename = "database", defaultValue = "password")
    private static String password;
    @LoadFromConfig(filename = "database", defaultValue = "MyHome")
    private static String databaseName;

    private static Connection connection;
    private static Statement statement;

    public static void init() {
        ConfigLoader.load(SQLConnection.class);
        try {
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            // Create database
            createDB(databaseName);
            connection.setCatalog(databaseName);
            statement = connection.createStatement();
        } catch (Exception e) {
            log.fatal("Can not connected to database");
            throw new RuntimeException(e);
        }
        VFrame.print("Database connected");
    }

    public static void stop() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static ResultSet executeQuery(String request) throws SQLException {
        return statement.executeQuery(request);
    }

    static int executeUpdate(String request) throws SQLException {
        return statement.executeUpdate(request);
    }

    static PreparedStatement insert(String table, String... columnNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `").append(table).append("` (");
        sb.append("`").append(columnNames[0]).append("`");
        for (int i = 1; i < columnNames.length; i++) {
            sb.append(",`").append(columnNames[i]).append("`");
        }
        sb.append(") VALUES (?");
        for (int i = 1; i < columnNames.length; i++) {
            sb.append(",?");
        }
        sb.append(");");

        try {
            return connection.prepareStatement(sb.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean createDB(String name) {
        try {
            executeUpdate(String.format("CREATE DATABASE `%s`;", name));
            return true;
        } catch (SQLException ignore) {
            return false;
        }
    }
}
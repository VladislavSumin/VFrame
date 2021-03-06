package ru.falseteam.vframe.sql;

import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.VFrameRuntimeException;
import ru.falseteam.vframe.config.ConfigLoader;
import ru.falseteam.vframe.config.LoadFromConfig;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Create single connection to database.
 * Send all requests to database.
 *
 * @author Evgeny Rudzyansky
 * @author Sumin Vladislav
 * @version 1.1
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SQLConnection {
    private static final Logger log = Logger.getLogger(SQLConnection.class.getName());

    @SuppressWarnings("SpellCheckingInspection")
    @LoadFromConfig(filename = "database",
            defaultValue = "jdbc:mysql://localhost:3306?useSSL=false&useUnicode=true&autoReconnect=true&characterEncoding=UTF-8")
    private static String url;
    @LoadFromConfig(filename = "database", defaultValue = "root")
    private static String username;
    @LoadFromConfig(filename = "database", defaultValue = "root")
    private static String password;
    @LoadFromConfig(filename = "database", defaultValue = "databaseName")
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
            throw new VFrameRuntimeException("VFrame: SQLConnection: Can not connected to database", e);
        }
        log.info("VFrame: Database connected");
    }

    public static void stop() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            //TODO вот если тут крашнется тогда обработать.
        }
    }

    public static ResultSet executeQuery(String request) throws SQLException {
        return statement.executeQuery(request);
    }

    public static int executeUpdate(String request) throws SQLException {
        return statement.executeUpdate(request);
    }

    public static PreparedStatement update(String table, String condition, String... columns) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE `").append(table).append("` SET `").append(columns[0]).append("` = ?");
        for (int i = 1; i < columns.length; ++i) sb.append(", `").append(columns[i]).append("` = ?");
        sb.append(' ').append(condition).append(" ;");
        return connection.prepareStatement(sb.toString());
    }

    public static PreparedStatement insert(String table, String... columns) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `").append(table).append("` (").append('`').append(columns[0]).append('`');
        for (int i = 1; i < columns.length; ++i) sb.append(", `").append(columns[i]).append('`');
        sb.append(") VALUES ( ?");
        for (int i = 1; i < columns.length; ++i) sb.append(", ?");
        sb.append(");");
        return connection.prepareStatement(sb.toString());
    }

    private static boolean createDB(String name) {
        try {
            executeUpdate(String.format("CREATE DATABASE `%s`;", name));
            return true;
        } catch (SQLException ignore) {
            return false;
        }
    }

    //TODO возможность нескольких соединений, путем создания не статичной копии класса со своими параметрами.
}
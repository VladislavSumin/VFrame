package ru.vladislavsumin.vframetest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;
import ru.vladislavsumin.vframe.config.ConfigLoader;
import ru.vladislavsumin.vframe.config.LoadFromConfig;
import ru.vladislavsumin.vframe.console.ConsoleWorker;
import ru.vladislavsumin.vframe.console.DefaultStopCommand;
import ru.vladislavsumin.vframe.serializable.DefaultPermissions;
import ru.vladislavsumin.vframe.socket.client.ClientSocketWorker;
import ru.vladislavsumin.vframe.socket.server.SSLServerSocketFactory;
import ru.vladislavsumin.vframe.socket.server.ServerSocketWorker;

import java.util.Map;

public class Main {
    private static final Logger log = LogManager.getLogger();

    @LoadFromConfig(defaultValue = "aaa")
    private static String pass;

    public static void main(String[] args) {
        log.trace("Test running");
        ConfigLoader.load(Main.class);

        SSLServerSocketFactory factory = new SSLServerSocketFactory("config/keystore.jks", "", pass);
        new ServerSocketWorker(Test.class, factory.createServerSocket(8889));
        new ClientSocketWorker("127.0.0.1", 8889, "config/keystore.jks", "").start();
    }

}

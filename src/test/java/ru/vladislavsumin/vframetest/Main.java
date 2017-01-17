package ru.vladislavsumin.vframetest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;
import ru.vladislavsumin.vframe.config.ConfigLoader;
import ru.vladislavsumin.vframe.config.LoadFromConfig;
import ru.vladislavsumin.vframe.console.ConsoleWorker;
import ru.vladislavsumin.vframe.socket.VFKeystore;
import ru.vladislavsumin.vframe.socket.client.ClientSocketWorker;
import ru.vladislavsumin.vframe.socket.server.ServerSocketWorker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class Main {
    private static final Logger log = LogManager.getLogger();

    @LoadFromConfig(defaultValue = "aaa")
    private static String pass;

    public static void main(String[] args) {
        log.trace("Test running");
        ConfigLoader.load(Main.class);
        VFrame.init();

        ConsoleWorker.addCommand(new testCommand());
        ConsoleWorker.startListenAsDaemon();

        VFKeystore factory = null;
        try {
            factory = new VFKeystore(new FileInputStream("config/keystore.jks"), "", pass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new ServerSocketWorker(Test.class, factory, 8888);
        new ClientSocketWorker("127.0.0.1", 8889, "config/keystore.jks", "").start();
    }

}

package ru.vladislavsumin.vframetest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;
import ru.vladislavsumin.vframe.console.ConsoleWorker;
import ru.vladislavsumin.vframe.console.DefaultStopCommand;
import ru.vladislavsumin.vframe.serializable.DefaultPermissions;

import java.util.Map;

public class Main {
    private static final Logger log = LogManager.getLogger();


    public static void main(String[] args) {
        log.trace("Test running");
        VFrame.init();
        ConsoleWorker.addCommand(new DefaultStopCommand(Main.class));
        ConsoleWorker.startListenAsDaemon();
        VFrame.stop();

        test();
    }

    public static void stop() {

    }

    private static void test() {
        Enum<? extends Enum<?>> e = DefaultPermissions.level0;
    }
}

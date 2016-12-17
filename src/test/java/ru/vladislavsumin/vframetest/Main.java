package ru.vladislavsumin.vframetest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;
import ru.vladislavsumin.vframe.console.ConsoleWorker;
import ru.vladislavsumin.vframe.console.DefaultStopCommand;

public class Main {
    private static final Logger log = LogManager.getLogger();


    public static void main(String[] args) {
        log.trace("Test running");
        VFrame.init();
        ConsoleWorker.addCommand(new DefaultStopCommand(Main.class));
        ConsoleWorker.startListenAsDaemon();
        VFrame.stop();
    }

    public static void stop(){

    }
}

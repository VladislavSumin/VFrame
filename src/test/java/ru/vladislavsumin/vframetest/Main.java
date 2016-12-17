package ru.vladislavsumin.vframetest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrame;

public class Main {
    private static final Logger log = LogManager.getLogger();


    public static void main(String[] args) {
        log.trace("Test running");
        VFrame.init();

        VFrame.stop();
    }
}

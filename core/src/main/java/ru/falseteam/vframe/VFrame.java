package ru.falseteam.vframe;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main library class.
 * Must be init before use another library classes.
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings("unused")
public class VFrame {
    private static final Logger log = Logger.getLogger(VFrame.class.getName());

    private static final Object lock = new Object();
    private static boolean init = false;

    private static Timer timer;

    /**
     * Initialize VFrame library.
     */
    public static void init() {
        synchronized (lock) {
            if (init) {
                throw new VFrameRuntimeException("VFrame already init");
            }

            // Init logger
            InputStream config = VFrame.class.getClassLoader().getResourceAsStream("logging.properties");
            try {
                LogManager.getLogManager().readConfiguration(config);
            } catch (IOException e) {
                e.printStackTrace(); //TODO fix this
            }

            init = true;
            timer = new Timer("VFrame: main timer");
            log.info("VFrame init");
        }
    }

    /**
     * Stop all internal library threads.
     */
    public static void stop() {
        synchronized (lock) {
            if (!init) {
                throw new VFrameRuntimeException("VFrame already not init");
            }
            init = false;
            timer.cancel();
            timer = null;
            log.info("VFrame stop");
        }
    }

    /**
     * Printing msg with String.format to console with time tag.
     *
     * @param msg    - msg to print
     * @param vararg - args from String.format
     */
    @Deprecated
    public static void print(String msg, Object... vararg) {
        print(String.format(msg, vararg));
    }

    /**
     * Printing msg to console with time tag.
     *
     * @param msg - msg to print
     */
    @Deprecated
    public static void print(String msg) {
        SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss.SSS] ");
        System.out.println(format.format(new Date()) + msg);
    }

    public static void addPeriodicalTimerTask(TimerTask task, long period) {
        synchronized (lock) {
            initError();
            timer.schedule(task, 0, period);
        }
    }

    public static void addPeriodicalTimerTask(TimerTask task, Date firstTime, long period) {
        synchronized (lock) {
            initError();
            timer.schedule(task, firstTime, period);
        }
    }

    public static void addEveryOneDayPeriodicalTask(TimerTask task, long timeOfDay) {
        synchronized (lock) {
            initError();
            long t = System.currentTimeMillis();
            t -= t % (1000 * 60 * 60 * 24) + 3 * 60 * 60 * 1000; //Magic
            timer.scheduleAtFixedRate(task, new Date(t + timeOfDay), 1000 * 60 * 60 * 24);
        }
    }

    private static void initError() {
        if (!init)
            throw new VFrameRuntimeException("VFrame not init");
    }
}

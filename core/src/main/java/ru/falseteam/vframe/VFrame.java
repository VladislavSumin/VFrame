package ru.falseteam.vframe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main library class.
 * Must be init before use another library classes.
 * <p>
 * Главный класс библиотеки.
 * Метод init() должен быть вызван до начала работы с библиотекой.
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings("unused")
public class VFrame {
    private static final Logger log = LogManager.getLogger();

    private static final Object lock = new Object();
    private static boolean init = false;

    private static Timer timer;

    /**
     * Initialize VFrame library.
     */
    public static void init() {
        synchronized (lock) {
            if (init) {
                log.error("VFrame already initialized");
                return;
            }
            init = true;
            timer = new Timer("VFrame main timer");
            print("VFrame initialized");
        }
    }

    /**
     * Stop all internal library threads.
     */
    public static void stop() {
        synchronized (lock) {
            if (!init) {
                log.error("VFrame already not initialized");
                return;
            }
            init = false;
            timer.cancel();
            timer = null;
            print("VFrame stopped");
        }
    }

    /**
     * Printing msg with String.format to console with time tag.
     *
     * @param msg    - msg to print
     * @param vararg - args from String.format
     */
    public static void print(String msg, Object... vararg) {
        print(String.format(msg, vararg));
    }

    /**
     * Printing msg to console with time tag.
     *
     * @param msg - msg to print
     */
    public static void print(String msg) {
        SimpleDateFormat format = new SimpleDateFormat("[HH:mm:ss.SSS] ");
        System.out.println(format.format(new Date()) + msg);
    }

    public static void addPeriodicalTimerTask(TimerTask task, long period) {
        synchronized (lock) {
            if (!init) {
                initError();
                return;
            }
            timer.schedule(task, 0, period);
        }
    }

    public static void addPeriodicalTimerTask(TimerTask task, Date firstTime, long period) {
        synchronized (lock) {
            if (!init) {
                initError();
                return;
            }
            timer.schedule(task, firstTime, period);
        }
    }

    public static void addEveryOneDayPeriodicalTask(TimerTask task, long timeOfDay) {
        synchronized (lock) {
            if (!init) {
                initError();
                return;
            }
            long t = System.currentTimeMillis();
            t -= t % (1000 * 60 * 60 * 24) + 3 * 60 * 60 * 1000; //Magic
            timer.scheduleAtFixedRate(task, new Date(t + timeOfDay), 1000 * 60 * 60 * 24);
        }
    }

    private static void initError() {
        final String error = "VFrame not init";
        log.fatal(error);
        throw new VFrameRuntimeException(error);
    }
}

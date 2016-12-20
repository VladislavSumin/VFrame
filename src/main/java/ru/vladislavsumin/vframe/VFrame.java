package ru.vladislavsumin.vframe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Main library class.
 * <p>
 * Must be init before use another library classes.
 *
 * @author Sumin Vladislav
 * @version 0.4
 */
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
            log.trace("VFrame initialized");
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
            log.trace("VFrame stopped");
        }
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

    public static boolean isInitialized() {
        return init;
    }

    private static void initError() {
        final String error = "VFrame not init";
        log.fatal(error);
        throw new VFrameRuntimeException(error);
    }
}

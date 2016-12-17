package ru.vladislavsumin.vframe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main library class.
 * <p>
 * Must be init before use another library classes.
 *
 * @author Sumin Vladislav
 * @version 0.2
 */
public class VFrame {
    private static final Logger log = LogManager.getLogger();

    private static final Object lock = new Object();
    private static boolean init = false;

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
            log.trace("VFrame stopped");
        }
    }

    public static boolean isInitialized() {
        return init;
    }
}

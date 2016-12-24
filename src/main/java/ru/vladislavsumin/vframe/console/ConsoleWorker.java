package ru.vladislavsumin.vframe.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Listen System.in. Receive and process data.
 *
 * @author Sumin Vladislav
 * @version 1.5
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConsoleWorker implements Runnable {
    private static Logger log = LogManager.getLogger();

    private static final Map<String, CommandInterface> commands = new HashMap<>();

    /**
     * Add command to command list.
     *
     * @param command - command
     */
    public static void addCommand(CommandInterface command) {
        commands.put(command.getName(), command);
    }

    /**
     * Start listen System.in as daemon
     */
    public static void startListenAsDaemon() {
        new ConsoleWorker();
    }

    private ConsoleWorker() {
        Thread thread = new Thread(this, "ConsoleWorker");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        log.trace("ConsoleWorker initialized");
        //noinspection InfiniteLoopStatement
        while (true) try {
            String[] data = reader.readLine().split(" ", 2);
            if (data[0].equals("")) continue; // Skip empty line
            if (commands.containsKey(data[0])) {
                if (data.length == 1)
                    commands.get(data[0]).exec("");
                else
                    commands.get(data[0]).exec(data[1]);
            } else
                System.out.println("Unknown command");
        } catch (IOException e) {
            log.fatal("VFrame: ConsoleWorker internal error");
            throw new VFrameRuntimeException(e);
        }
    }
}

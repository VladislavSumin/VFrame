package ru.falseteam.vframe.console;

import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.VFrameRuntimeException;
import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Listen System.in
 * Receive and process data.
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings({"unused"})
public class ConsoleWorker {
    private static Logger log = Logger.getLogger(ConsoleWorker.class.getName());

    private static final Map<String, CommandAbstract> commands = new HashMap<>();

    /**
     * Add command to command list.
     *
     * @param command - command
     */
    public static void addCommand(CommandAbstract command) {
        if (commands.size() == 0) {
            // Run listener thread
            Thread thread = new Thread(run, "VFrame: ConsoleWorker");
            thread.setDaemon(true);
            thread.start();
        }
        // Check if command already exists
        if (commands.containsKey(command.getName()))
            throw new VFrameRuntimeException(
                    String.format("VFrame: Command with name %s already exists", command.getName()));
        commands.put(command.getName(), command);
    }

    private static final Runnable run = new Runnable() {
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            log.info("VFrame: ConsoleWorker initialized");
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
                throw new VFrameRuntimeException("VFrame: ConsoleWorker internal error", e);
            }
        }
    };

}

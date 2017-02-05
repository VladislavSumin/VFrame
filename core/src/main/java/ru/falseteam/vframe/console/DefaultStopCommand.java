package ru.falseteam.vframe.console;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default command to stop server.
 *
 * @author Sumin Vladislav
 * @version 2.0
 */
public class DefaultStopCommand extends CommandAbstract {
    private static final Logger log = LogManager.getLogger();

    public interface Stoppable {
        void stop();
    }

    private final Stoppable stop;

    public DefaultStopCommand(Stoppable stop) {
        this.stop = stop;
    }

    @Override
    public void exec(CommandLine commandLine) {
        stop.stop();
    }

    @Override
    public String getName() {
        return "stop";
    }
}

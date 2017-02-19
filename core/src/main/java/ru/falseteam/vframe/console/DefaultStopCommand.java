package ru.falseteam.vframe.console;

import org.apache.commons.cli.CommandLine;

/**
 * Default command to stop server.
 *
 * @author Sumin Vladislav
 * @version 2.2
 */
@SuppressWarnings("unused")
public class DefaultStopCommand extends CommandAbstract {
    @SuppressWarnings("WeakerAccess")
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

package ru.falseteam.vframe.console;

import org.apache.commons.cli.CommandLine;
import ru.falseteam.vframe.VFrameRuntimeException;

/**
 * Default command to stop server.
 *
 * @author Sumin Vladislav
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
        try {
            stop.stop();
        } catch (Exception e) {
            throw new VFrameRuntimeException("VFrame: Exception on stop command", e);
        }
    }

    @Override
    public String getName() {
        return "stop";
    }
}

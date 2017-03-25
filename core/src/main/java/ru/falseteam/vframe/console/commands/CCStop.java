package ru.falseteam.vframe.console.commands;

import org.apache.commons.cli.CommandLine;
import ru.falseteam.vframe.VFrameRuntimeException;
import ru.falseteam.vframe.console.CommandAbstract;

/**
 * Default command to stop server.
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings("unused")
public class CCStop extends CommandAbstract {
    @SuppressWarnings("WeakerAccess")
    public interface Stoppable {
        void stop();
    }

    private final Stoppable stop;

    public CCStop(Stoppable stop) {
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

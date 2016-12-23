package ru.vladislavsumin.vframe.console;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default command to stop server.
 *
 * @author Sumin Vladislav
 * @version 1.1
 */
public class DefaultStopCommand extends CommandAbstract {
    private static final Logger log = LogManager.getLogger();

    private final Method method;

    public DefaultStopCommand(Class<?> clazz) {
        try {
            this.method = clazz.getMethod("stop");
        } catch (NoSuchMethodException e) {
            log.fatal("Class {} hav not method stop", clazz.getName());
            throw new VFrameRuntimeException(e);
        }
    }

    @Override
    public void exec(CommandLine commandLine) {
        try {
            method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.fatal("VFame internal err", e);
            throw new VFrameRuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "stop";
    }
}

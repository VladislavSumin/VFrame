package ru.vladislavsumin.vframe.console;

import org.apache.commons.cli.CommandLine;

/**
 * @author Sumin Vladislav
 * @version 1.1
 */
@SuppressWarnings("WeakerAccess")
public interface CommandInterface {
    String getName();

    void exec(String param);

    void exec(CommandLine commandLine);
}

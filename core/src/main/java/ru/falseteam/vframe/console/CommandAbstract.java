package ru.falseteam.vframe.console;

import org.apache.commons.cli.*;

/**
 * Base abstract command realization
 *
 * @author Sumin Vladislav
 * @version 2.5
 */
@SuppressWarnings({"unused"})
public abstract class CommandAbstract {
    private final Options options = new Options();

    final void exec(String param) {
        try {
            CommandLine commandLine = new DefaultParser().parse(options, param.split(" "));
            exec(commandLine);
        } catch (ParseException e) {
            showHelp();
        }
    }

    /**
     * Process command code here
     *
     * @param commandLine - command arguments
     */
    protected abstract void exec(CommandLine commandLine);

    @SuppressWarnings("WeakerAccess")
    protected final void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getName(), options);
    }

    protected final void addOption(String shortOptions, String longOptions,
                                    int argsCount, boolean required, String descriptions) {
        boolean hasArgs = argsCount != 0;
        Option option = new Option(shortOptions, longOptions, hasArgs, descriptions);
        option.setArgs(argsCount);
        option.setRequired(required);
        option.setArgName(longOptions);
        options.addOption(option);
    }

    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }
}

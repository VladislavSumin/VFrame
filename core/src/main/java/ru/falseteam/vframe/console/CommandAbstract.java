package ru.falseteam.vframe.console;

import org.apache.commons.cli.*;

/**
 * Base abstract command realization
 *
 * @author Sumin Vladislav
 * @version 2.5
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class CommandAbstract {
    private Options options = new Options();

    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }

    public final void exec(String param) {
        try {
            CommandLine commandLine = new DefaultParser().parse(options, param.split(" "));
            exec(commandLine);
        } catch (ParseException e) {
            showHelp();
        }
    }

    protected abstract void exec(CommandLine commandLine);

    protected final void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getName(), options);
    }

    protected final void addOptions(String shortOptions, String longOptions,
                                    int argsCount, boolean required, String descriptions) {
        boolean hasArgs = argsCount != 0;
        Option option = new Option(shortOptions, longOptions, hasArgs, descriptions);
        option.setArgs(argsCount);
        option.setRequired(required);
        option.setArgName(longOptions);
        options.addOption(option);
    }
}

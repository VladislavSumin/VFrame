package ru.vladislavsumin.vframetest;

import org.apache.commons.cli.CommandLine;
import ru.vladislavsumin.vframe.console.CommandAbstract;

/**
 * Created by admin on 24.12.2016.
 */
public class testCommand extends CommandAbstract {
    public testCommand() {
        addOptions("t","test",1,true,"test function");
    }

    @Override
    public void exec(CommandLine commandLine) {

    }
}

package ru.falseteam.vframe.console.commands;

import org.apache.commons.cli.CommandLine;
import ru.falseteam.vframe.VFrame;
import ru.falseteam.vframe.console.CommandAbstract;

/**
 * Print uptime
 *
 * @author Sumin Vladislav
 */
public class CCUptime extends CommandAbstract {
    @Override
    protected void exec(CommandLine commandLine) {
        long uptime = System.currentTimeMillis() - VFrame.getUptime();
        StringBuilder sb = new StringBuilder();
        sb.append("Uptime: ");

        long days = uptime / (1000 * 60 * 60 * 24);
        long hour = uptime % (1000 * 60 * 60 * 24) / (1000 * 60 * 60);
        long minutes = uptime % (1000 * 60 * 60) / (1000 * 60);
        long sec = uptime % (1000 * 60) / (1000);

        if (days > 0)
            sb.append(days).append("d");

        if (hour > 0 || days > 0) {
            if (hour < 10) sb.append('0');
            sb.append(hour).append("h");
        }

        if (minutes > 0 || hour > 0 || days > 0) {
            if (minutes < 10) sb.append('0');
            sb.append(minutes).append("m");
        }

        if (sec < 10) sb.append('0');
        sb.append(sec).append("s");

        System.out.println(sb.toString());
    }

    @Override
    public String getName() {
        return "uptime";
    }
}

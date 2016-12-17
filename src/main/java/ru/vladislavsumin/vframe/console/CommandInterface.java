package ru.vladislavsumin.vframe.console;

/**
 * @author Sumin Vladislav
 * @version 1.1
 */
@SuppressWarnings("WeakerAccess")
public interface CommandInterface {
    String getName();

    void exec(String param);
}

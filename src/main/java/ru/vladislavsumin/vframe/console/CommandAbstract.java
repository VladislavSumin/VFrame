package ru.vladislavsumin.vframe.console;

/**
 * Base abstract command realization
 *
 * @author Sumin Vladislav
 * @version 1.1
 */
@SuppressWarnings("unused")
public abstract class CommandAbstract implements CommandInterface {
    @Override
    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }
}

package ru.vladislavsumin.vframe.socket.server;

import java.util.Arrays;
import java.util.List;

/**
 * Realize base server protocol methods
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public abstract class ServerProtocolAbstract implements ServerProtocolInterface {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}

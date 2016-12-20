package ru.vladislavsumin.vframe.socket.client;

/**
 * Realize base client protocol methods
 *
 * @author Sumin Vladislav
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public abstract class ClientProtocolAbstract implements ClientProtocolInterface {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}

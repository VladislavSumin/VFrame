package ru.falseteam.vframe;

/**
 * Default VFrame runtime exception
 * All library classes can throw this exception on error
 *
 * @author Sumin Vladislav
 */
@SuppressWarnings("unused")
public class VFrameRuntimeException extends RuntimeException {
    public VFrameRuntimeException() {
    }

    public VFrameRuntimeException(String message) {
        super(message);
    }

    public VFrameRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public VFrameRuntimeException(Throwable cause) {
        super(cause);
    }
}


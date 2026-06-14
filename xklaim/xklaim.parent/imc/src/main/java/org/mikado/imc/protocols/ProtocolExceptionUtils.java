package org.mikado.imc.protocols;

import java.io.EOFException;
import java.net.SocketException;

/**
 * Utility methods for inspecting exception cause chains that include
 * {@link ProtocolException}, which stores its wrapped exception in a custom
 * field rather than the standard Java cause chain.
 * 
 * @author Lorenzo Bettini
 */
public class ProtocolExceptionUtils {

    private ProtocolExceptionUtils() {
        // utility class
    }

    /**
     * Returns {@code true} if the given throwable, or any exception in its
     * cause chain (including inside {@link ProtocolException} wrappers), is an
     * {@link EOFException} or a {@link SocketException} with the message
     * {@code "Socket closed"}.
     *
     * <p>Both of these indicate a normal connection close rather than an
     * unexpected protocol error.
     *
     * @param t the throwable to inspect
     * @return {@code true} if caused by a connection-close condition
     */
    public static boolean isCausedByConnectionClose(Throwable t) {
        while (t != null) {
            if (t instanceof EOFException)
                return true;
            if (t instanceof SocketException
                    && "Socket closed".equals(t.getMessage()))
                return true;
            if (t instanceof ProtocolException protocolException) {
                t = protocolException.represents();
            } else {
                t = t.getCause();
            }
        }
        return false;
    }
}

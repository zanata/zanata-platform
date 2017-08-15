// org.jboss.seam.core.LockTimeoutException from Seam 2.3.1
package org.zanata.exception;

/**
 * Thrown by the SynchronizationInterceptor when it fails to get a lock within the allocated time.
 *
 * @author Shane Bryzak
 */
public class LockTimeoutException extends Exception {
    private static final long serialVersionUID = 7008592409909366676L;

    public LockTimeoutException() {
        super();
    }

    public LockTimeoutException(String message) {
        super(message);
    }

    public LockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

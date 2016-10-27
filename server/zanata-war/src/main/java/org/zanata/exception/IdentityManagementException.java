package org.zanata.exception;

/**
 * Thrown when an exception is encountered during account/role creation.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class IdentityManagementException extends RuntimeException {
    private static final long serialVersionUID = -1900327754519392577L;

    public IdentityManagementException(String message) {
        super(message);
    }

    public IdentityManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.zanata.exception;

public class ActivationLinkExpiredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ActivationLinkExpiredException() {
        super();
    }

    public ActivationLinkExpiredException(String message) {
        super(message);
    }

}

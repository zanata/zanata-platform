package org.zanata.client.exceptions;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InvalidUserInputException extends RuntimeException {

    private static final long serialVersionUID = -3782362811204642666L;

    public InvalidUserInputException() {
        super();
    }

    public InvalidUserInputException(String message) {
        super(message);
    }
}

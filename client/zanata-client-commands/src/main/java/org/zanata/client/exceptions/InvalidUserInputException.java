package org.zanata.client.exceptions;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InvalidUserInputException extends RuntimeException {

    public InvalidUserInputException() {
        super();
    }

    public InvalidUserInputException(String message) {
        super(message);
    }
}

package org.zanata.rest;

public class ReadOnlyEntityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReadOnlyEntityException() {
    }

    public ReadOnlyEntityException(String message) {
        super(message);
    }

}

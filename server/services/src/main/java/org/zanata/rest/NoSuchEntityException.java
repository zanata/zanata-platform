package org.zanata.rest;

public class NoSuchEntityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoSuchEntityException() {
    }

    public NoSuchEntityException(String message) {
        super(message);
    }

}

package org.zanata.exception;

public class ZanataServiceException extends ZanataException {
    /**
    *
    */
    private static final long serialVersionUID = 1L;

    private int httpStatus = 500; // Internal server error by default

    public ZanataServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ZanataServiceException(Throwable e) {
        super(e.getMessage(), e);
    }

    public ZanataServiceException(String message) {
        super(message);
    }

    public ZanataServiceException(String message, int httpStatus, Throwable e) {
        super(message, e);
        this.httpStatus = httpStatus;
    }

    public ZanataServiceException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * @return The http status that is suggested be used to inform about this
     *         service exception.
     */
    public int getHttpStatus() {
        return this.httpStatus;
    }

}

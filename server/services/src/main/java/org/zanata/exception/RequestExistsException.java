package org.zanata.exception;

/**
 * Exception of when duplicate request is being created.
 *
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class RequestExistsException extends ZanataServiceException {

    private static final long serialVersionUID = 3996435872028208341L;

    public RequestExistsException(String message) {
        super(message);
    }
}

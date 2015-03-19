package org.zanata.exception;

import org.jboss.resteasy.spi.BadRequestException;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InvalidDateParamException extends BadRequestException {
    private static final long serialVersionUID = -3758176883692074605L;

    public InvalidDateParamException(String message) {
        super(message);
    }
}

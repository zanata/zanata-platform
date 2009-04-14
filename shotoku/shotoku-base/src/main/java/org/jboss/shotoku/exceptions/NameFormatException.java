package org.jboss.shotoku.exceptions;

/**
 * Thrown when the user attempts to create a resource with an invalid name,
 * for example, one which contains a space.
 * @author Adam Warski (adamw@aster.pl)
 */
public class NameFormatException extends Exception {
    public NameFormatException(Exception e) {
		super(e);
	}

	public NameFormatException(String msg) {
		super(msg);
	}

	public NameFormatException() {

	}
}

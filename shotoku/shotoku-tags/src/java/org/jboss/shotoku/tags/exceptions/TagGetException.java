package org.jboss.shotoku.tags.exceptions;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TagGetException extends Exception {
    public TagGetException(Throwable e) { super(e); }
    public TagGetException(String message, Throwable e) { super(message, e); }
    public TagGetException(String message) { super(message); }
}

package org.jboss.shotoku.tags.exceptions;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TagAddException extends Exception {
    public TagAddException(Throwable e) { super(e); }
    public TagAddException(String message, Throwable e) { super(message, e); }
    public TagAddException(String message) { super(message); }
}

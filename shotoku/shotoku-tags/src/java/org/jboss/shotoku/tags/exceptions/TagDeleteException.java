package org.jboss.shotoku.tags.exceptions;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TagDeleteException extends Exception {
    public TagDeleteException(Throwable e) { super(e); }
    public TagDeleteException(String message, Throwable e) { super(message, e); }
    public TagDeleteException(String message) { super(message); }
}

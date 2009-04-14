package org.jboss.shotoku.cache;

/**
 * Describes the possible type of changes which can be made on a
 * resource.
 * @author Adam Warski (adamw@aster.pl)
 */
public enum ChangeType {
    MODIFIED,
    ADDED,
    DELETED,
    NONE
}

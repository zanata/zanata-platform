package org.jboss.shotoku.service;

/**
 * A class that represents a "null" value - it is needed as nulls can't be
 * stored as values in a ConcurrentHashMap.
 * @author Adam Warski (adamw@aster.pl)
 */
public class Null {
    private Null() { }

    private static Null instance = new Null();
    public static Null getInstance() { return instance; }

    public boolean equals(Object obj) {
        return obj instanceof Null;
    }

    public int hashCode() {
        return 0;
    }
}

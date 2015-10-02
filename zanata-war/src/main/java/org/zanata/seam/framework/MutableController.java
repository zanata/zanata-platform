// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import java.io.Serializable;

/**
 * Base class for controllers which implement the Mutable interface.
 *
 * @author Gavin King
 */
public abstract class MutableController<T>
        extends Controller
        implements Serializable {
    private static final long serialVersionUID = -3074038114884198501L;
    // copy/paste from AbstractMutable

    private transient boolean dirty;

    /**
     * Set the dirty flag if the value has changed. Call whenever a subclass
     * attribute is updated.
     *
     * @param oldValue
     *            the old value of an attribute
     * @param newValue
     *            the new value of an attribute
     * @return true if the newValue is not equal to the oldValue
     */
    protected <U> boolean setDirty(U oldValue, U newValue) {
        boolean attributeDirty = oldValue != newValue && (
                oldValue == null ||
                !oldValue.equals(newValue)
                );
        dirty = dirty || attributeDirty;
        return attributeDirty;
    }

}

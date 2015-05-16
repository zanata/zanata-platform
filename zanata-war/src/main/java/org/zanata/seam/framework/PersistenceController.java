// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

/**
 * Base class for controller objects which require a persistence context object.
 *
 * @param <T>
 *            the persistence context class (eg. Session or EntityManager)
 * @author Gavin King
 */
public abstract class PersistenceController<T> extends Controller {
    private transient T persistenceContext;

    public T getPersistenceContext() {
        if (persistenceContext == null) {
            persistenceContext =
                    (T) getComponentInstance(getPersistenceContextName());
        }
        return persistenceContext;
    }

    public void setPersistenceContext(T persistenceContext) {
        this.persistenceContext = persistenceContext;
    }

    protected abstract String getPersistenceContextName();

}

// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import javax.persistence.EntityManager;
import javax.transaction.SystemException;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.transaction.Transaction;

/**
 * Base class for Home objects of JPA entities.
 *
 * @author Gavin King
 */
public class EntityHome<E> extends Home<EntityManager, E> {
    private static final long serialVersionUID = -3140094990727574632L;

    /**
     * Run on {@link EntityHome} instantiation. <br />
     * Validates that an {@link EntityManager} is available.
     *
     * @see Home#create()
     */
    @Override
    public void create() {
        super.create();
        if (getEntityManager() == null) {
            throw new IllegalStateException("entityManager is null");
        }
    }

    /**
     * Returns true if the entity instance is managed
     */
    @Transactional
    public boolean isManaged() {
        return getInstance() != null &&
                getEntityManager().contains(getInstance());
    }

    /**
     * Flush any changes made to the managed entity instance to the underlying
     * database. <br />
     * If the update is successful, a log message is printed, a
     * {@link javax.faces.application.FacesMessage} is added and a transaction
     * success event raised.
     *
     * @return "updated" if the update is successful
     * @see Home#updatedMessage()
     * @see Home#raiseAfterTransactionSuccessEvent()
     */
    @Transactional
    public String update() {
        joinTransaction();
        getEntityManager().flush();
        updatedMessage();
        raiseAfterTransactionSuccessEvent();
        return "updated";
    }

    /**
     * Persist unmanaged entity instance to the underlying database. If the
     * persist is successful, a log message is printed, a
     * {@link javax.faces.application.FacesMessage } is added and a transaction
     * success event raised.
     *
     * @return "persisted" if the persist is successful
     * @see Home#createdMessage()
     * @see Home#raiseAfterTransactionSuccessEvent()
     */
    @Transactional
    public String persist() {
        getEntityManager().persist(getInstance());
        getEntityManager().flush();
        assignId(PersistenceProvider.instance().getId(getInstance(),
                getEntityManager()));
        createdMessage();
        raiseAfterTransactionSuccessEvent();
        return "persisted";
    }

    /**
     * Remove managed entity instance from the Persistence Context and the
     * underlying database. If the remove is successful, a log message is
     * printed, a {@link javax.faces.application.FacesMessage} is added and a
     * transaction success event raised.
     *
     * @return "removed" if the remove is successful
     * @see Home#deletedMessage()
     * @see Home#raiseAfterTransactionSuccessEvent()
     */
    @Transactional
    public String remove() {
        getEntityManager().remove(getInstance());
        getEntityManager().flush();
        deletedMessage();
        raiseAfterTransactionSuccessEvent();
        return "removed";
    }

    /**
     * Implementation of {@link Home#find() find()} for JPA
     *
     * @see Home#find()
     */
    @Transactional
    @Override
    public E find() {
        if (getEntityManager().isOpen()) {
            E result = loadInstance();
            if (result == null) {
                result = handleNotFound();
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Utility method to load entity instance from the {@link EntityManager}.
     * Called by {@link #find()}. <br />
     * Can be overridden to support eager fetching of associations.
     *
     * @return The entity identified by {@link Home#getEntityClass()
     *         getEntityClass()}, {@link Home#getId() getId()}
     */
    protected E loadInstance() {
        return getEntityManager().find(getEntityClass(), getId());
    }

    /**
     * Implementation of {@link Home#joinTransaction() joinTransaction()} for
     * JPA.
     */
    @Override
    protected void joinTransaction() {
        if (getEntityManager().isOpen()) {
            try {
                Transaction.instance().enlist(getEntityManager());
            } catch (SystemException se) {
                throw new RuntimeException("could not join transaction", se);
            }
        }
    }

    /**
     * The Seam Managed Persistence Context used by this Home component
     */
    public EntityManager getEntityManager() {
        return getPersistenceContext();
    }

    /**
     * The Seam Managed Persistence Context used by this Home component.
     */
    public void setEntityManager(EntityManager entityManager) {
        setPersistenceContext(entityManager);
    }

    /**
     * The name the Seam component managing the Persistence Context. <br />
     * Override this or {@link #getEntityManager()} if your persistence context
     * is not named <code>entityManager</code>.
     */
    @Override
    protected String getPersistenceContextName() {
        return "entityManager";
    }

    /**
     * Implementation of {@link Home#getEntityName() getEntityName()} for JPA
     *
     * @see Home#getEntityName()
     */
    @Override
    protected String getEntityName() {
        try {
            return PersistenceProvider.instance().getName(getInstance(),
                    getEntityManager());
        } catch (IllegalArgumentException e) {
            // Handle that the passed object may not be an entity
            return null;
        }
    }

}

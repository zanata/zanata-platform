// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.model.ModelEntityBase;
import org.zanata.ui.faces.FacesMessages;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;

/**
 * Base class for components which provide persistence operations to a JPA-
 * managed entity instance. This class may be reused by either configuration
 * or extension, and may be bound directly to a view, or accessed by some
 * intermediate Seam component.
 *
 * Base class for Home objects of JPA entities.
 *
 * @author Gavin King
 */
public abstract class EntityHome<E extends ModelEntityBase> implements
        Serializable {
    private static final Logger log = LoggerFactory.getLogger(EntityHome.class);
    private static final long serialVersionUID = 1L;

    private Object id;
    private E instance;
    // NonNull: This will be initialised at PostConstruct time
    private Class<E> entityClass;

    @Inject
    private UserTransaction transaction;

    @Inject
    private EntityManager entityManager;

    @Inject
    private FacesMessages facesMessages;

    /**
     * Run on {@link Home} instantiation to check the Home component is in a
     * valid state. <br />
     * Validates that the class of the entity to be managed has been specified.
     */
    @PostConstruct
    public void postConstruct() {
        if (getEntityClass() == null) {
            throw new IllegalStateException("entityClass is null");
        }
    }

    /**
     * Get the id of the object being managed.
     */
    public Object getId() {
        return id;
    }

    /**
     * Set/change the entity being managed by id.
     *
     */
    public void setId(Object id) {
        setInstance(null);
        this.id = id;
    }

    /**
     * Set the id of entity being managed. <br />
     * Does not alter the instance so used if the id of the managed object is
     * changed.
     *
     */
    protected void assignId(Object id) {
        this.id = id;
    }

    /**
     * Returns true if the id of the object managed is known (eg from a
     * servlet request parameter). Note that this might be true for one
     * request but not for another, during the ViewScoped lifetime of the
     * EntityHome object (so the instance should remember its id).
     */
    public boolean isIdDefined() {
        return getId() != null && !"".equals(getId());
    }

    /**
     * Get the managed entity, using the id from {@link #getId()} to load, it
     * from the Persistence Context, refreshing it if disconnected,
     * or creating a new instance if the id is not
     * defined. Because our EntityManagers have shorter scopes than EntityHome, we
     * reload the entity on each request to ensure it is attached.
     *
     * @see #getId()
     */
    @Transactional
    public @Nonnull E getInstance() {
        joinTransaction();
        if (instance == null) {
            if (isIdDefined()) {
                this.instance = loadInstanceByExternalId();
            } else {
                this.instance = createInstance();
            }
        } else if (instance.getId() != null) {
            if (!entityManager.contains(instance)) {
                E result = entityManager.find(getEntityClass(), instance.getId());
                if (result == null) {
                    handleNotFound();
                } else {
                    this.instance = result;
                }
            }
        }
        return instance;
    }

    /**
     * Set/change the entity being managed.
     */
    protected void setInstance(E instance) {
        this.instance = instance;
    }

    private E loadInstanceByExternalId() {
        if (!isTransactionMarkedRollback()) {
            // we cache the instance so that it does not "disappear"
            // after remove() is called on the instance
            // is this really a Good Idea??
            return find();
        } else {
            throw new RuntimeException("transaction rolled back");
        }
    }

    /**
     * Create a new instance of the entity. <br />
     * Utility method called by {@link #initInstance()} to create a new instance
     * of the entity.
     */
    protected E createInstance() {
        assert entityClass != null;
        try {
            return entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the entity instance is managed
     */
    @Transactional
    public boolean isManaged() {
        return getEntityManager().contains(getInstance());
    }

    /**
     * Flush any changes made to the managed entity instance to the underlying
     * database. <br />
     * If the update is successful, a log message is printed, a
     * {@link javax.faces.application.FacesMessage} is added and a transaction
     * success event raised.
     *
     * @return "updated" if the update is successful
     */
    @Transactional
    public String update() {
        joinTransaction();
        // The EntityManager is RequestScoped, so merge the old instance
        // (ViewScoped) before saving:
        E mergedInstance = getEntityManager().merge(getInstance());
        setInstance(mergedInstance);
        getEntityManager().flush();
        updatedMessage();
        return "updated";
    }

    /**
     * Persist unmanaged entity instance to the underlying database. If the
     * persist is successful, a log message is printed, a
     * {@link javax.faces.application.FacesMessage } is added and a transaction
     * success event raised.
     *
     * @return "persisted" if the persist is successful
     */
    @Transactional
    public String persist() {
        getEntityManager().persist(getInstance());
        getEntityManager().flush();
        E instance = getInstance();
        Long id = instance.getId();
        assignId(id);

        createdMessage();
        return "persisted";
    }

    /**
     * Remove managed entity instance from the Persistence Context and the
     * underlying database. If the remove is successful, a log message is
     * printed, a {@link javax.faces.application.FacesMessage} is added and a
     * transaction success event raised.
     *
     * @return "removed" if the remove is successful
     */
    @Transactional
    public String remove() {
        getEntityManager().remove(getInstance());
        getEntityManager().flush();
        deletedMessage();
        return "removed";
    }

    /**
     * Add a {@link javax.faces.application.FacesMessage} and log a message when
     * the entity instance is updated.
     * <p/>
     * Utility method to add a {@link javax.faces.application.FacesMessage} from
     * the Seam managed resource bundle and log the entity when the
     * managed entity is updated.
     *
     */
    protected void updatedMessage() {
        log.debug("updated entity {} {}", getEntityClass().getName(), getId());
        getFacesMessages().addGlobal(
                FacesMessage.SEVERITY_INFO, "Successfully updated");
    }

    private FacesMessages getFacesMessages() {
        return facesMessages;
    }

    /**
     * Add a {@link javax.faces.application.FacesMessage} and log a message when
     * the entity instance is deleted.
     * <p/>
     * Utility method to add a {@link javax.faces.application.FacesMessage} from
     * the Seam managed resource bundle and log the entity when the
     * managed entity is deleted.
     *
     */
    protected void deletedMessage() {
        log.debug("deleted entity {} {}", getEntityClass().getName(), getId());
        getFacesMessages().addGlobal(
                FacesMessage.SEVERITY_INFO, "Successfully deleted");

    }

    /**
     * Add a {@link javax.faces.application.FacesMessage} and log a message when
     * the entity instance is created.
     * <p/>
     * Utility method to add a {@link javax.faces.application.FacesMessage} from
     * the Seam managed resource bundle and log the entity when the
     * managed entity is updated.
     *
     */
    protected void createdMessage() {
        log.debug("created entity {} {}", getEntityClass().getName(), getId());
        getFacesMessages().addGlobal(
                FacesMessage.SEVERITY_INFO, "Successfully created");

    }

    /**
     * Hook method called by {@link #initInstance()} to allow the implementation
     * to load the entity from the Persistence Context.
     */
    @Transactional
    public @Nonnull E find() {
        if (getEntityManager().isOpen()) {
            E result = loadInstance();
            if (result == null) {
                result = handleNotFound();
            }
            return result;
        } else {
            throw new RuntimeException("EntityManager is closed");
        }
    }

    /**
     * Utility method called by the framework when no entity is found in the
     * Persistence Context.
     */
    protected E handleNotFound() {
        throw new EntityNotFoundException(String.format(
                "entity not found: %s#%s", getEntityClass(), getId()));
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

    protected boolean isTransactionMarkedRollback() {
        try {
            return getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hook method called to allow the implementation to join the current
     * transaction when necessary.
     */
    protected void joinTransaction() {
        if (getEntityManager().isOpen()) {
            try {
                UserTransaction transaction = getTransaction();
                int status = transaction.getStatus();
                boolean isActiveOrMarkedRollback = status == STATUS_ACTIVE || status == STATUS_MARKED_ROLLBACK;
                if (isActiveOrMarkedRollback) {
                    getEntityManager().joinTransaction();
                }
            } catch (SystemException se) {
                throw new RuntimeException("could not join transaction", se);
            }
        }
    }

    private UserTransaction getTransaction() {
        return transaction;
    }

    /**
     * The EntityManager used by this Home component
     */
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get the class of the entity being managed. <br />
     * If not explicitly specified, the generic type of implementation is used.
     */
    public Class<E> getEntityClass() {
        if (entityClass == null) {
            // CDI will return a proxy instance (so need an extra getSuperClass)
            Type type = getClass().getSuperclass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                if (paramType.getActualTypeArguments().length == 2) {
                    // likely dealing with -> new
                    // EntityHome<Person>().getEntityClass()
                    if (paramType
                            .getActualTypeArguments()[1] instanceof TypeVariable) {
                        throw new IllegalArgumentException(
                                "Could not guess entity class by reflection");
                    } else {
                        // likely dealing with -> new Home<EntityManager, Person>()
                        // { ... }.getEntityClass()
                        entityClass =
                                (Class<E>) paramType
                                        .getActualTypeArguments()[1];
                    }
                } else {
                    // likely dealing with -> new PersonHome().getEntityClass()
                    // where PersonHome extends EntityHome<Person>
                    entityClass =
                            (Class<E>) paramType.getActualTypeArguments()[0];
                }
            } else {
                throw new IllegalArgumentException(
                        "Could not guess entity class by reflection");
            }
        }
        return entityClass;
    }

    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

}

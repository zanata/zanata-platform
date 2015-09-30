// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.faces.application.FacesMessage;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ServiceLocator;

/**
 * Base class for components which provide persistence operations to a managed
 * entity instance. This class may be reused by either configuration or
 * extension, and may be bound directly to a view, or accessed by some
 * intermediate Seam component.
 *
 * @author Gavin King
 */
@Scope(ScopeType.CONVERSATION)
public abstract class Home<T, E> extends MutableController<T> {
    private static final Logger log = LoggerFactory.getLogger(Home.class);
    private static final long serialVersionUID = -5462396456614090423L;

    private Object id;
    protected E instance;
    private Class<E> entityClass;


    /**
     * Add a {@link javax.faces.application.FacesMessage} and log a message when
     * the entity instance is updated.
     * <p/>
     * Utility method to add a {@link javax.faces.application.FacesMessage} from
     * the Seam managed resource bundle or, if not specified in the resource
     * bundle, from {@link #getUpdatedMessage()} and log the entity when the
     * managed entity is updated.
     *
     */
    protected void updatedMessage() {
        log.debug("updated entity {} {}", getEntityClass().getName(), getId());
        getFacesMessages().addGlobal(
                FacesMessage.SEVERITY_INFO, "Successfully updated");
    }

    private FacesMessages getFacesMessages() {
        return ServiceLocator.instance().getInstance(FacesMessages.class);
    }

    /**
     * Add a {@link javax.faces.application.FacesMessage} and log a message when
     * the entity instance is deleted.
     * <p/>
     * Utility method to add a {@link javax.faces.application.FacesMessage} from
     * the Seam managed resource bundle or, if not specified in the resource
     * bundle, from {@link #getDeletedMessage()} and log the entity when the
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
     * the Seam managed resource bundle or, if not specified in the resource
     * bundle, from {@link #getUpdatedMessage()} and log the entity when the
     * managed entity is updated.
     *
     */
    protected void createdMessage() {
        log.debug("created entity {} {}", getEntityClass().getName(), getId());
        getFacesMessages().addGlobal(
                FacesMessage.SEVERITY_INFO, "Successfully created");

    }

    /**
     * Run on {@link Home} instantiation to check the Home component is in a
     * valid state. <br />
     * Validates that the class of the entity to be managed has been specified.
     */
    @Create
    public void create() {
        if (getEntityClass() == null) {
            throw new IllegalStateException("entityClass is null");
        }
    }

    /**
     * Get the managed entity, using the id from {@link #getId()} to load it
     * from the Persistence Context or creating a new instance if the id is not
     * defined.
     *
     * @see #getId()
     */
    @Transactional
    public E getInstance() {
        joinTransaction();
        if (instance == null) {
            initInstance();
        }
        return instance;
    }

    /**
     * Load the instance if the id is defined otherwise create a new instance <br />
     * Utility method called by {@link #getInstance()} to load the instance from
     * the Persistence Context if the id is defined. Otherwise a new instance is
     * created.
     *
     * @see #find()
     * @see #createInstance()
     */
    protected void initInstance() {
        if (isIdDefined()) {
            if (!isTransactionMarkedRollback()) {
                // we cache the instance so that it does not "disappear"
                // after remove() is called on the instance
                // is this really a Good Idea??
                setInstance(find());
            }
        } else {
            setInstance(createInstance());
        }
    }

    /**
     * Hook method called to allow the implementation to join the current
     * transaction when necessary.
     */
    protected void joinTransaction() {
    }

    /**
     * Hook method called by {@link #initInstance()} to allow the implementation
     * to load the entity from the Persistence Context.
     */
    protected E find() {
        return null;
    }

    /**
     * Utility method called by the framework when no entity is found in the
     * Persistence Context.
     */
    protected E handleNotFound() {
        throw new EntityNotFoundException(getId(), getEntityClass());
    }

    /**
     * Create a new instance of the entity. <br />
     * Utility method called by {@link #initInstance()} to create a new instance
     * of the entity.
     */
    protected E createInstance() {
        if (getEntityClass() != null) {
            try {
                return getEntityClass().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    /**
     * Get the class of the entity being managed. <br />
     * If not explicitly specified, the generic type of implementation is used.
     */
    public Class<E> getEntityClass() {
        if (entityClass == null) {
            Type type = getClass().getGenericSuperclass();
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
        if (setDirty(this.id, id))
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
        setDirty(this.id, id);
        this.id = id;
    }

    /**
     * Returns true if the id of the object managed is known.
     */
    public boolean isIdDefined() {
        return getId() != null && !"".equals(getId());
    }

    /**
     * Set/change the entity being managed.
     */
    public void setInstance(E instance) {
        setDirty(this.instance, instance);
        this.instance = instance;
    }

}

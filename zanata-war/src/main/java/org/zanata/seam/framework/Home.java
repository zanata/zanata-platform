// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import static org.jboss.seam.international.StatusMessage.Severity.INFO;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.framework.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected ValueExpression<T> newInstance;

    private ValueExpression deletedMessage;
    private ValueExpression createdMessage;
    private ValueExpression updatedMessage;

    /**
     * Add a {@link javax.faces.application.FacesMessage} and log a message when
     * the entity instance is updated.
     * <p/>
     * Utility method to add a {@link javax.faces.application.FacesMessage} from
     * the Seam managed resource bundle or, if not specified in the resource
     * bundle, from {@link #getUpdatedMessage()} and log the entity when the
     * managed entity is updated.
     *
     * @see #getUpdatedMessage()
     * @see #getUpdatedMessageKey()
     */
    protected void updatedMessage() {
        log.debug("updated entity {} {}", getEntityClass().getName(), getId());
        getStatusMessages().addFromResourceBundleOrDefault(INFO,
                getUpdatedMessageKey(),
                getUpdatedMessage().getExpressionString());
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
     * @see #getDeletedMessage()
     * @see #getDeletedMessageKey()
     */
    protected void deletedMessage() {
        log.debug("deleted entity {} {}", getEntityClass().getName(), getId());
        getStatusMessages().addFromResourceBundleOrDefault(INFO,
                getDeletedMessageKey(),
                getDeletedMessage().getExpressionString());
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
     * @see #getCreatedMessage()
     * @see #getCreatedMessageKey()
     */
    protected void createdMessage() {
        log.debug("created entity {} {}", getEntityClass().getName(), getId());
        getStatusMessages().addFromResourceBundleOrDefault(INFO,
                getCreatedMessageKey(),
                getCreatedMessage().getExpressionString());
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
        initDefaultMessages();
    }

    protected void initDefaultMessages() {
        Expressions expressions = new Expressions();
        if (createdMessage == null) {
            createdMessage =
                    expressions.createValueExpression("Successfully created");
        }
        if (updatedMessage == null) {
            updatedMessage =
                    expressions.createValueExpression("Successfully updated");
        }
        if (deletedMessage == null) {
            deletedMessage =
                    expressions.createValueExpression("Successfully deleted");
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
     * Clear the managed entity (and id), allowing the {@link EntityHome} to be
     * reused.
     */
    public void clearInstance() {
        setInstance(null);
        setId(null);
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
        if (newInstance != null) {
            return (E) newInstance.getValue();
        } else if (getEntityClass() != null) {
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
     * Set the class of the entity being managed. <br />
     * Useful for configuring {@link Home} components from
     * <code>components.xml</code>.
     */
    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
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
     * @see #assignId(Object)
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
     * @see #setId(Object)
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

    /**
     * {@link javax.el.ValueExpression} to execute to load a new instance. <br />
     * Mainly used when configuring the {@link Home} components in
     * <code>components.xml</code>.
     */
    public ValueExpression getNewInstance() {
        return newInstance;
    }

    /**
     * {@link javax.el.ValueExpression} to execute to load a new instance. <br />
     * Mainly used when configuring the {@link Home} components in
     * <code>components.xml</code>.
     */
    public void setNewInstance(ValueExpression newInstance) {
        this.newInstance = newInstance;
    }

    /**
     * Message displayed to user when the managed entity is created.
     */
    public ValueExpression getCreatedMessage() {
        return createdMessage;
    }

    /**
     * Message displayed to user when the managed entity is created.
     */
    public void setCreatedMessage(ValueExpression createdMessage) {
        this.createdMessage = createdMessage;
    }

    /**
     * Message displayed to user when the managed entity is deleted.
     */
    public ValueExpression getDeletedMessage() {
        return deletedMessage;
    }

    /**
     * Message displayed to user when the managed entity is deleted.
     */
    public void setDeletedMessage(ValueExpression deletedMessage) {
        this.deletedMessage = deletedMessage;
    }

    /**
     * Message displayed to user when the managed entity is updated.
     */
    public ValueExpression getUpdatedMessage() {
        return updatedMessage;
    }

    /**
     * Message displayed to user when the managed entity is updated.
     */
    public void setUpdatedMessage(ValueExpression updatedMessage) {
        this.updatedMessage = updatedMessage;
    }

    /**
     * The prefix of the key to look up messages in the Seam managed resource
     * bundle. <br />
     * By default the simple name of the class suffixed with an underscore.
     */
    protected String getMessageKeyPrefix() {
        String className = getEntityClass().getName();
        return className.substring(className.lastIndexOf('.') + 1) + '_';
    }

    /**
     * The key to look up in the Seam managed resource bundle the message
     * displayed when the managed entity is created. <br />
     * By default the {@link #getMessageKeyPrefix()} suffixed with created.
     */
    protected String getCreatedMessageKey() {
        return getMessageKeyPrefix() + "created";
    }

    /**
     * The key to look up in the Seam managed resource bundle the message
     * displayed when the managed entity is updated. <br />
     * By default the {@link #getMessageKeyPrefix()} suffixed with updated.
     */
    protected String getUpdatedMessageKey() {
        return getMessageKeyPrefix() + "updated";
    }

    /**
     * The key to look up in the Seam managed resource bundle the message
     * displayed when the managed entity is deleted. <br />
     * By default the {@link #getMessageKeyPrefix()} suffixed with deleted.
     */
    protected String getDeletedMessageKey() {
        return getMessageKeyPrefix() + "deleted";
    }

    /**
     * Raise events when a CRUD operation succeeds. <br />
     * Utility method to raise two events: an event of type
     * <code>org.jboss.seam.afterTransactionSuccess</code> is raised, along with
     * an event of type
     * <code>org.jboss.seam.afterTransactionSuccess.&lt;entityName&gt;</code>.
     */
    protected void raiseAfterTransactionSuccessEvent() {
        raiseTransactionSuccessEvent("org.jboss.seam.afterTransactionSuccess");
        String simpleEntityName = getSimpleEntityName();
        if (simpleEntityName != null) {
            raiseTransactionSuccessEvent("org.jboss.seam.afterTransactionSuccess."
                    + simpleEntityName);
        }
    }

    /**
     * The simple name of the managed entity
     */
    protected String getSimpleEntityName() {
        String name = getEntityName();
        if (name != null) {
            return name.lastIndexOf(".") > 0
                    && name.lastIndexOf(".") < name.length() ? name.substring(
                    name.lastIndexOf(".") + 1, name.length()) : name;
        } else {
            return null;
        }
    }

    /**
     * Hook method to get the name of the managed entity
     */
    protected abstract String getEntityName();

}

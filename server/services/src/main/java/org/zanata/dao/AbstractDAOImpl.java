package org.zanata.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import javax.inject.Inject;

/**
 * Based on code from http://community.jboss.org/wiki/GenericDataAccessObjects
 */
public class AbstractDAOImpl<T, ID extends Serializable> implements
        GenericDAO<T, ID>, Serializable {
    private static final long serialVersionUID = 1L;

    private Class<T> persistentClass;
    private Session session;

    protected AbstractDAOImpl() {
    }

    public AbstractDAOImpl(Class<T> clz, Session session) {
        this(clz);
        this.session = session;
    }

    public AbstractDAOImpl(Class<T> clz) {
        this.persistentClass = clz;
    }

    @Inject
    public void setSession(Session s) {
        this.session = s;
    }

    protected Session getSession() {
        if (session == null) {
            throw new IllegalStateException(
                    "Session has not been set on DAO before usage");
        }
        return session;
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    /**
     * Use session.get() instead of session.load() to prevent non-null entity
     * being return if does not exists.
     *
     * See {@link org.hibernate.Session#get(Class, Serializable)} and
     * {@link org.hibernate.Session#load(Class, Serializable)} for documentation.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T findById(ID id, boolean lock) {
        T entity;
        if (lock) {
            entity =
                    (T) getSession().get(getPersistentClass(), id,
                            LockOptions.UPGRADE);
        } else {
            entity = (T) getSession().get(getPersistentClass(), id);
        }

        return entity;
    }

    public T findById(ID id) {
        return findById(id, false);
    }

    @Override
    public List<T> findAll() {
        return findByCriteria();
    }

    @Override
    public void deleteAll() {
        for (T t : findAll()) {
            getSession().delete(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example = Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    @Override
    public T makePersistent(T entity) {
        getSession().saveOrUpdate(entity);
        return entity;
    }

    @Override
    public void makeTransient(T entity) {
        getSession().delete(entity);
    }

    @Override
    public void flush() {
        getSession().flush();
    }

    @Override
    public void clear() {
        getSession().clear();
    }

    @Override
    public boolean isPersistent(T entity) {
        return getSession().contains(entity);

    };

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
    }

}

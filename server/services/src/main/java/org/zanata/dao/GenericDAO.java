package org.zanata.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Based on code from http://community.jboss.org/wiki/GenericDataAccessObjects
 */
public interface GenericDAO<T, ID extends Serializable> {

    int MAX_PARAMS_IN_IN_CLAUSE = 50;

    T findById(ID id, boolean lock);

    void deleteAll();

    List<T> findAll();

    T makePersistent(T entity);

    void makeTransient(T entity);

    void flush();

    void clear();

    List<T> findByExample(T exampleInstance, String[] excludeProperty);

    boolean isPersistent(T entity);
}

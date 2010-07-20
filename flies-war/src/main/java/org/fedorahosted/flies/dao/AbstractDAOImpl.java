package org.fedorahosted.flies.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.jboss.seam.annotations.In;

/**
 * Based on code from http://community.jboss.org/wiki/GenericDataAccessObjects
 */
public abstract class AbstractDAOImpl<T, ID extends Serializable> implements GenericDAO<T, ID>
{

   private Class<T> persistentClass;
   private Session session;

   public AbstractDAOImpl(Class<T> clz, Session session)
   {
      this(clz);
      this.session = session;
   }

   public AbstractDAOImpl(Class<T> clz)
   {
      this.persistentClass = clz;
   }

   @In
   public void setSession(Session s)
   {
      this.session = s;
   }

   protected Session getSession()
   {
      if (session == null)
         throw new IllegalStateException("Session has not been set on DAO before usage");
      return session;
   }

   public Class<T> getPersistentClass()
   {
      return persistentClass;
   }

   @SuppressWarnings("unchecked")
   @Override
   public T findById(ID id, boolean lock)
   {
      T entity;
      if (lock)
         entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
      else
         entity = (T) getSession().load(getPersistentClass(), id);

      return entity;
   }

   @Override
   public List<T> findAll()
   {
      return findByCriteria();
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<T> findByExample(T exampleInstance, String[] excludeProperty)
   {
      Criteria crit = getSession().createCriteria(getPersistentClass());
      Example example = Example.create(exampleInstance);
      for (String exclude : excludeProperty)
      {
         example.excludeProperty(exclude);
      }
      crit.add(example);
      return crit.list();
   }

   @Override
   public T makePersistent(T entity)
   {
      getSession().saveOrUpdate(entity);
      return entity;
   }

   @Override
   public void makeTransient(T entity)
   {
      getSession().delete(entity);
   }

   @Override
   public void flush()
   {
      getSession().flush();
   }

   @Override
   public void clear()
   {
      getSession().clear();
   }

   @Override
   public boolean isPersistent(T entity)
   {
      return getSession().contains(entity);

   };

   /**
    * Use this inside subclasses as a convenience method.
    */
   @SuppressWarnings("unchecked")
   protected List<T> findByCriteria(Criterion... criterion)
   {
      Criteria crit = getSession().createCriteria(getPersistentClass());
      for (Criterion c : criterion)
      {
         crit.add(c);
      }
      return crit.list();
   }

}

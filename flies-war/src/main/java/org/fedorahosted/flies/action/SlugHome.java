package org.fedorahosted.flies.action;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.framework.EntityHome;

/**
 * This implementation uses a field 'slug' to refer to the id of the object.
 * 
 * @author asgeirf
 */
public abstract class SlugHome<E> extends EntityHome<E>
{

   private static final long serialVersionUID = -992545184963409194L;

   @Override
   protected E loadInstance()
   {
      Session session = (Session) getEntityManager().getDelegate();
      return (E) session.createCriteria(getEntityClass()).add(getNaturalId()).uniqueResult();
   }

   public abstract NaturalIdentifier getNaturalId();

   @Override
   public abstract boolean isIdDefined();

   @Override
   public abstract Object getId();

}

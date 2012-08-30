/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.model.validator;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.validator.Validator;
import org.jboss.seam.Component;

/**
 * Unique validator implementation.
 * NB: <b>Requires Seam and Hibernate</b>.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see {@link Unique}
 */
public class UniqueValidator implements Validator<Unique>
{
   private Unique parameters;

   @Override
   public boolean isValid(Object value)
   {
      if( value == null )
      {
         return true;
      }
      return countRows( value ) == 0;
   }

   private int countRows(Object value)
   {
      EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
      Session session = (Session)entityManager.getDelegate();

      ClassMetadata metadata = session.getSessionFactory().getClassMetadata( value.getClass() );
      String idName = metadata.getIdentifierPropertyName();
      Serializable id = metadata.getIdentifier(value, EntityMode.POJO);

      DetachedCriteria criteria = DetachedCriteria.forClass( value.getClass() );
      for( String property : parameters.properties() )
      {
         criteria.add(Restrictions.eq( property, metadata.getPropertyValue(value, property, EntityMode.POJO) ));
      }

      // Id property
      if( id != null )
      {
         criteria.add( Restrictions.ne( idName, id ) );
      }
      criteria.setProjection(Projections.rowCount());

      // change the flush mode temporarily to perform the query or else incomplete entities will try to get flushed
      // After the query, go back to the original mode
      FlushMode flushMode = session.getFlushMode();
      session.setFlushMode(FlushMode.MANUAL);
      List results = criteria.getExecutableCriteria( session ).list();
      Number count = (Number)results.iterator().next();
      session.setFlushMode(flushMode);
      return count.intValue();
   }

   @Override
   public void initialize(Unique parameters)
   {
      this.parameters = parameters;
   }
}

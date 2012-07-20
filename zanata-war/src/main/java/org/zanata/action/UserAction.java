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
package org.zanata.action;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.management.IdentityManager;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 * Extension of Seam management's UserAction class' behaviour.
 *
 * @see {@link org.jboss.seam.security.management.action.UserAction}
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("org.jboss.seam.security.management.userAction")
@Scope(CONVERSATION)
@Install(precedence = APPLICATION)
public class UserAction extends org.jboss.seam.security.management.action.UserAction
{
   @In
   private IdentityManager identityManager;

   @In
   private EntityManager entityManager;

   @In
   private Map<String, String> messages;

   public void deleteUser( String userName )
   {
      try
      {
         identityManager.deleteUser(userName);
         // NB: Need to call flush here to be able to catch the persistence exception, otherwise it would be caught by Seam.
         entityManager.flush();
      }
      catch (PersistenceException e)
      {
         if( e.getCause() instanceof ConstraintViolationException)
         {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, messages.get("jsf.UserManager.delete.constraintViolation.error") );
         }
      }
   }
}

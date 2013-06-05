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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.dao.PersonDAO;
import org.zanata.service.UserAccountService;

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
   private static final long serialVersionUID = 1L;

   @In
   private IdentityManager identityManager;

   @In
   private EntityManager entityManager;

   @In
   private Map<String, String> messages;

   @In
   private PersonDAO personDAO;

   @In
   private UserAccountService userAccountServiceImpl;

   @In(create = true)
   private Renderer renderer;

   private boolean newUserFlag;

   private String originalUsername;

   private String usernameFilter;


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

   public String getEmail(String username)
   {
      return personDAO.findEmail(username);
   }

   public String getName(String username)
   {
      return personDAO.findByUsername(username).getName();
   }

   public String getUsernameFilter()
   {
      return usernameFilter;
   }

   public void setUsernameFilter(String usernameFilter)
   {
      this.usernameFilter = usernameFilter;
   }

   @Override
   @Begin
   public void createUser()
   {
      super.createUser();
      newUserFlag = true;
   }

   @Override
   @Begin
   public void editUser(String username)
   {
      super.editUser(username);
      newUserFlag = false;
      originalUsername = username;
   }

   @Override
   public String save()
   {
      boolean usernameChanged = false;

      // Allow user name changes when editing
      if( !newUserFlag && !originalUsername.equals(getUsername()) )
      {
         if( isNewUsernameValid(getUsername()) )
         {
            userAccountServiceImpl.editUsername(originalUsername, getUsername());
            usernameChanged = true;
         }
         else
         {
            FacesMessages.instance().addToControl("username", messages.get("jsf.UsernameNotAvailable"));
            setUsername(originalUsername); // reset the username field
            return "failure";
         }
      }

      String saveResult = super.save();

      if( usernameChanged )
      {
         renderer.render("/WEB-INF/facelets/email/username_changed.xhtml");
      }
      return saveResult;
   }

   /**
    * Validate that a user name is not already in the system, by another account
    */
   private boolean isNewUsernameValid(String username)
   {
      try
      {
         entityManager.createQuery("from HAccount a where a.username = :username").setParameter("username", username).getSingleResult();
         return false;
      }
      catch (NoResultException e)
      {
         // pass
         return true;
      }
   }
}

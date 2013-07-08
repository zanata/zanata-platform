/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.model.HAccount;
import org.zanata.util.ZanataMessages;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("languageJoinUpdateRoleAction")
@Scope(ScopeType.PAGE)
public class LanguageJoinUpdateRoleAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private static final String JOIN_REQUEST = "join";
   
   private static final String ROLE_REQUEST = "role";
   
   
   @In
   private ZanataMessages zanataMessages;
   
   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;
   
   @Getter
   @Setter
   private boolean requestAsTranslator;
   
   @Getter
   @Setter
   private boolean requestAsReviewer;
   
   @Getter
   @Setter
   private boolean requestAsCoordinator;
   
   @Getter
   @Setter
   private String requestType;
   
   @Setter
   private String language;
   
   private String roleMessage;
   
   private String title;
   
   private String subject;
   
   
   public boolean hasRoleRequest()
   {
      return requestAsTranslator || requestAsReviewer || requestAsCoordinator;
   }
   
   
   public String getSubject()
   {
      if(requestType.equals(JOIN_REQUEST))
      {
         subject = zanataMessages.getMessage("jsf.email.joinrequest.Subject");
      }
      else
      {
         subject = zanataMessages.getMessage("jsf.email.rolerequest.Subject");
      }
      return subject;
   }
   
   public String getTitle()
   {
      if(requestType.equals(JOIN_REQUEST))
      {
         title = zanataMessages.getMessage("jsf.RequestToJoinLanguageTeamTitle");
      }
      else
      {
         title = zanataMessages.getMessage("jsf.RequestRoleLanguageTeamTitle");
      }
      return title;
   }
   
   public String getRoleMessage()
   {
      StringBuilder sb = new StringBuilder();
      
      sb.append(zanataMessages.getMessage("jsf.email.joinrequest.Subject"));
      
      if(requestAsTranslator || requestAsReviewer || requestAsCoordinator)
      {
         sb.append(" as");
         if(requestAsTranslator)
         {
            sb.append(" (");
            sb.append(zanataMessages.getMessage("jsf.Translator"));
            sb.append(" )");
         }
         
         if(requestAsReviewer)
         {
            sb.append(" (");
            sb.append(zanataMessages.getMessage("jsf.Reviewer"));
            sb.append(" )");
         }
         
         if(requestAsCoordinator)
         {
            sb.append(" (");
            sb.append(zanataMessages.getMessage("jsf.Coordinator"));
            sb.append(" )");
         }
      }
      roleMessage = sb.toString();
      return roleMessage;
   }

}

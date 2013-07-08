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

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.util.ZanataMessages;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("languageJoinAction")
@Scope(ScopeType.PAGE)
public class LanguageJoinAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @In
   private ZanataMessages zanataMessages;
   
   private String roleMessage;
   
   @Getter
   @Setter
   private boolean joinAsTranslator;
   
   @Getter
   @Setter
   private boolean joinAsReviewer;
   
   @Getter
   @Setter
   private boolean joinAsCoordinator;
   
   
   public boolean hasRoleRequest()
   {
      return joinAsTranslator || joinAsReviewer || joinAsCoordinator;
   }
   
   
   public String getRoleMessage()
   {
      StringBuilder sb = new StringBuilder();
      
      sb.append(zanataMessages.getMessage("jsf.email.joinrequest.Subject"));
      
      if(joinAsTranslator || joinAsReviewer || joinAsCoordinator)
      {
         sb.append(" as");
         sb.append( joinAsTranslator ? " (Translator) " : "");
         sb.append( joinAsReviewer ? " (Reviewer) " : "");
         sb.append( joinAsCoordinator ? " (Coordinator) " : "");
      }
      roleMessage = sb.toString();
      
      return roleMessage;
   }

}

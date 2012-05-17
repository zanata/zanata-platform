/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.server.TranslationWorkspace;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface SecurityService
{
   /**
    * This will check permission for performing an action upon translations with given project and locale
    *
    * @param workspace workspace
    * @param projectSlug project slug
    * @param localeId localeId
    * @param action translation action
    * @throws org.jboss.seam.security.AuthorizationException, org.jboss.seam.security.NotLoggedInException
    */
   void checkPermissionForTranslation(TranslationWorkspace workspace, String projectSlug, LocaleId localeId, TranslationAction action);

   public enum TranslationAction
   {
      // security actions (to be implemented)
      ADD("add-translation"),
      MODIFY("modify-translation"),
      REMOVE("remove-translation"),
      APPROVE("approve-translation");

      private final String action;

      private TranslationAction(String action)
      {
         this.action = action;
      }

      public String action()
      {
         return action;
      }
   }
}

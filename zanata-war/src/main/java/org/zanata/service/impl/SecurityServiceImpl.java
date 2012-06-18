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

package org.zanata.service.impl;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.AuthorizationException;
import org.zanata.common.LocaleId;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("securityServiceImpl")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class SecurityServiceImpl implements SecurityService
{
   @In
   ProjectDAO projectDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   ZanataIdentity identity;

   @In
   private TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public SecurityCheckResult checkPermission(AbstractWorkspaceAction action, TranslationAction translationAction) throws NoSuchWorkspaceException
   {
      identity.checkLoggedIn();

      WorkspaceId workspaceId = action.getWorkspaceId();
      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(workspaceId);
      LocaleId localeId = workspaceId.getLocaleId();
      ProjectIterationId projectIterationId = workspaceId.getProjectIterationId();
      if (workspace.getWorkspaceContext().isReadOnly())
      {
         throw new AuthorizationException("Project or version is read-only");
      }

      String projectSlug = projectIterationId.getProjectSlug();
      HProject hProject = projectDAO.getBySlug(projectSlug);
      HLocale hLocale = localeServiceImpl.getByLocaleId(localeId);
      identity.checkPermission(translationAction.action(), hLocale, hProject);

      return new SecurityCheckResultImpl(hLocale, workspace);
   }

   private static class SecurityCheckResultImpl implements SecurityCheckResult
   {
      private final HLocale hLocale;
      private final TranslationWorkspace workspace;

      private SecurityCheckResultImpl(HLocale hLocale, TranslationWorkspace workspace)
      {
         this.hLocale = hLocale;
         this.workspace = workspace;
      }

      @Override
      public HLocale getLocale()
      {
         return hLocale;
      }

      @Override
      public TranslationWorkspace getWorkspace()
      {
         return workspace;
      }
   }
}

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

import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface SecurityService {

    /**
     * This will check permission for performing an action upon translations
     * with given project and locale
     *
     * @param action
     *            abstract workspace action which contains locale id and project
     *            slug
     * @param translationAction
     *            translation action enum (at the moment only supports MODIFY)
     * @throws org.jboss.seam.security.AuthorizationException
     *             , org.jboss.seam.security.NotLoggedInException
     *             org.zanata.webtrans.shared.NoSuchWorkspaceException
     */
    SecurityCheckResult checkPermission(AbstractWorkspaceAction action,
            TranslationAction translationAction)
            throws NoSuchWorkspaceException;

    HProject checkWorkspaceStatus(WorkspaceId workspaceId);

    public enum TranslationAction {
        // security actions (to be implemented)
        ADD("add-translation"), MODIFY("modify-translation"), REMOVE(
                "remove-translation"), REVIEW("review-translation");

        private final String action;

        private TranslationAction(String action) {
            this.action = action;
        }

        public String action() {
            return action;
        }
    }

    interface SecurityCheckResult {
        HLocale getLocale();

        TranslationWorkspace getWorkspace();
    }
}

/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.model.HProject;
import org.zanata.model.type.WebhookType;
import org.zanata.service.impl.ProjectServiceImpl;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ProjectService extends Serializable {

    /**
     * Update all security settings for a person.
     * <p>
     * The HPerson and HLocale entities in memberships must be attached to avoid
     * persistence problems with Hibernate.
     */
    List<ProjectServiceImpl.UpdatedRole> updateProjectPermissions(HProject project,
        PersonProjectMemberships memberships);

    @Transactional
    boolean updateWebhook(HProject project, Long webhookId, String url,
            String secret, String name, Set<WebhookType> types);

    @Transactional
    boolean addWebhook(HProject project, String url, String secret,
            String name, Set<WebhookType> types);

    /**
     * Check if project contains duplicate webhook with matching url
     */
    boolean isDuplicateWebhookUrl(HProject project, String url);

    /**
     * Check if project contains duplicate webhook with matching url other than
     * given webhookId
     */
    boolean isDuplicateWebhookUrl(HProject project, String url, Long webhookId);

    void updateLocalePermissions(HProject project, PersonProjectMemberships memberships);
}

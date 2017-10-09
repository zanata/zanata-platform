/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.dao;

import java.util.List;

import org.hibernate.Session;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.zanata.model.WebHook;
import org.zanata.model.type.WebhookType;

@Named("webHookDAO")
@RequestScoped
public class WebHookDAO extends AbstractDAOImpl<WebHook, Long> {

    private static final long serialVersionUID = -1182286946609575021L;

    public WebHookDAO() {
        super(WebHook.class);
    }

    public WebHookDAO(Session session) {
        super(WebHook.class, session);
    }

    /**
     * Get a list of webhook from a project which has the given type set up.
     * @param projectSlug project slug
     * @param type webhoot type
     */
    @SuppressWarnings("unchecked")
    public List<WebHook> getWebHooksForType(String projectSlug,
            WebhookType type) {
        // This generates a warning in log but as per https://hibernate.atlassian.net/browse/HHH-10621
        // the warning message is a hibernate issue
        return getSession().createQuery(
                "from WebHook w where w.project.slug = :projectSlug and :webhookType in elements(w.types) ")
                .setParameter("projectSlug", projectSlug)
                .setParameter("webhookType", type)
                .setCacheable(true).setComment("getWebHooksForType").list();
    }
}

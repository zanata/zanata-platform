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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.model.tm.TransMemory;
import org.zanata.seam.framework.EntityHome;
import org.zanata.service.SlugEntityService;
import org.zanata.ui.faces.FacesMessages;

/**
 * Controller class for the Translation Memory UI.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("translationMemoryHome")
@RequestScoped
@Model
@Transactional
@CheckRole("admin")
public class TranslationMemoryHome extends EntityHome<TransMemory> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationMemoryHome.class);
    private static final long serialVersionUID = -8557363011909155662L;
    @Inject
    private SlugEntityService slugEntityServiceImpl;
    @Inject
    private FacesMessages facesMessages;

    public TranslationMemoryHome() {
        setEntityClass(TransMemory.class);
    }

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!slugEntityServiceImpl.isSlugAvailable(slug, TransMemory.class)) {
            facesMessages.addToControl(componentId, "This Id is not available");
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public String persist() {
        if (!validateSlug(getInstance().getSlug(), "slug")) {
            return null;
        }
        return super.persist();
    }
}

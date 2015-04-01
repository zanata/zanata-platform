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

import javax.faces.event.ValueChangeEvent;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityHome;
import org.zanata.model.tm.TransMemory;
import org.zanata.service.SlugEntityService;
import org.zanata.ui.faces.FacesMessages;

/**
 * Controller class for the Translation Memory UI.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationMemoryHome")
@Restrict("#{s:hasRole('admin')}")
@Slf4j
public class TranslationMemoryHome extends EntityHome<TransMemory> {
    @In
    private SlugEntityService slugEntityServiceImpl;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!slugEntityServiceImpl.isSlugAvailable(slug, TransMemory.class)) {
            facesMessages.addToControl(componentId,
                    "This Id is not available");
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

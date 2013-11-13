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
package org.zanata.action;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HProjectIteration;
import org.zanata.service.SlugEntityService;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHome")
public class VersionGroupHome extends SlugHome<HIterationGroup> {
    private static final long serialVersionUID = 1L;

    private String slug;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;

    @In
    SlugEntityService slugEntityServiceImpl;

    private List<SelectItem> statusList;

    @Override
    protected HIterationGroup loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HIterationGroup) session.byNaturalId(HIterationGroup.class)
                .using("slug", getSlug()).load();
    }

    public List<HProjectIteration> getInstanceProjectIterations() {
        return new ArrayList<HProjectIteration>(getInstance()
                .getProjectIterations());
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", slug);
    }

    @Override
    public boolean isIdDefined() {
        return slug != null;
    }

    @Override
    public Object getId() {
        return slug;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void validateSuppliedId() {
        getInstance(); // this will raise an EntityNotFound exception
        // when id is invalid and conversation will not
        // start
    }

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            FacesMessages.instance().addToControl(componentId,
                    "This Group ID is not available");
            return false;
        }
        return true;
    }

    public boolean isSlugAvailable(String slug) {
        return slugEntityServiceImpl.isSlugAvailable(slug,
                HIterationGroup.class);
    }

    @Override
    public String persist() {
        if (!validateSlug(getInstance().getSlug(), "slug"))
            return null;

        if (authenticatedAccount != null) {
            getInstance().addMaintainer(authenticatedAccount.getPerson());
        }

        return super.persist();
    }

    @Override
    public String update() {
        String state = super.update();
        return state;
    }

    public String cancel() {
        return "cancel";
    }

    @Override
    public List<SelectItem> getStatusList() {
        return getAvailableStatus();
    }

    private List<SelectItem> getAvailableStatus() {
        if (statusList == null) {
            statusList =
                    ImmutableList.copyOf(Iterables.filter(
                            super.getStatusList(), new Predicate<SelectItem>() {
                                @Override
                                public boolean apply(SelectItem input) {
                                    return !input.getValue().equals(
                                            EntityStatus.READONLY);
                                }
                            }));
        }
        return statusList;
    }
}

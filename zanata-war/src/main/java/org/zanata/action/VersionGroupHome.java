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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.service.SlugEntityService;
import org.zanata.service.VersionGroupService;
import org.zanata.service.impl.VersionGroupServiceImpl;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.autocomplete.LocaleAutocomplete;
import org.zanata.ui.autocomplete.MaintainerAutocomplete;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ComparatorUtil;
import org.zanata.util.ServiceLocator;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHome")
public class VersionGroupHome extends SlugHome<HIterationGroup> {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String slug;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private SlugEntityService slugEntityServiceImpl;

    @In
    private Messages msgs;

    @In
    private ConversationScopeMessages conversationScopeMessages;

    @Getter
    private GroupMaintainerAutocomplete maintainerAutocomplete =
            new GroupMaintainerAutocomplete();

    @Getter
    private VersionAutocomplete versionAutocomplete = new VersionAutocomplete();

    @Getter
    private GroupLocaleAutocomplete localeAutocomplete =
            new GroupLocaleAutocomplete();

    @Getter
    private AbstractListFilter<HPerson> maintainerFilter =
            new InMemoryListFilter<HPerson>() {
                @Override
                protected List<HPerson> fetchAll() {
                    return getInstanceMaintainers();
                }

                @Override
                protected boolean include(HPerson elem, String filter) {
                    return StringUtils.containsIgnoreCase(elem.getName(),
                            filter);
                }
            };

    public void verifySlugAvailable(ValueChangeEvent e) {
        String slug = (String) e.getNewValue();
        validateSlug(slug, e.getComponent().getId());
    }

    public boolean validateSlug(String slug, String componentId) {
        if (!isSlugAvailable(slug)) {
            facesMessages.addToControl(componentId,
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
    protected void updatedMessage() {
        // Disable the default message from Seam
    }

    @Override
    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public String persist() {
        if (!validateSlug(getInstance().getSlug(), "slug"))
            return null;

        if (authenticatedAccount != null) {
            getInstance().addMaintainer(authenticatedAccount.getPerson());
        }
        return super.persist();
    }

    @Override
    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public String update() {
        return super.update();
    }

    /**
     * This is for autocomplete components of which ConversationScopeMessages
     * will be null
     *
     * @param conversationScopeMessages
     * @return
     */
    private String update(ConversationScopeMessages conversationScopeMessages) {
        if (this.conversationScopeMessages == null) {
            this.conversationScopeMessages = conversationScopeMessages;
        }
        return update();
    }

    public void setStatus(char initial) {
        getInstance().setStatus(EntityStatus.valueOf(initial));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void removeLanguage(HLocale locale) {
        getInstance().getActiveLocales().remove(locale);
        update();
        conversationScopeMessages.setMessage(
                FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.LanguageRemoveFromGroup",
                        locale.retrieveDisplayName()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void removeVersion(HProjectIteration version) {
        getInstance().getProjectIterations().remove(version);
        update();
        conversationScopeMessages.setMessage(
                FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.VersionRemoveFromGroup", version.getSlug(),
                        version.getProject().getSlug()));
    }

    @Restrict("#{s:hasPermission(versionGroupHome.instance, 'update')}")
    public void removeMaintainer(HPerson maintainer) {
        if (getInstance().getMaintainers().size() <= 1) {
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.get("jsf.group.NeedAtLeastOneMaintainer"));
        } else {
            getInstance().removeMaintainer(maintainer);
            maintainerFilter.reset();
            update();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.MaintainerRemoveFromGroup",
                            maintainer.getName()));
        }
    }

    public List<HLocale> getInstanceActiveLocales() {
        List<HLocale> activeLocales =
                Lists.newArrayList(getInstance().getActiveLocales());

        Collections.sort(activeLocales, ComparatorUtil.LOCALE_COMPARATOR);
        return activeLocales;
    }

    @Override
    protected HIterationGroup loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HIterationGroup) session.byNaturalId(HIterationGroup.class)
                .using("slug", getSlug()).load();
    }

    // sort by slug
    public List<HProjectIteration> getSortedInstanceProjectIterations() {
        List<HProjectIteration> list =
                Lists.newArrayList(getInstance().getProjectIterations());

        Collections.sort(list, ComparatorUtil.PROJECT_NAME_COMPARATOR);

        return list;
    }

    public List<HPerson> getInstanceMaintainers() {
        List<HPerson> list = Lists.newArrayList(getInstance().getMaintainers());

        Collections.sort(list, ComparatorUtil.PERSON_NAME_COMPARATOR);

        return list;
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

    public void validateSuppliedId() {
        getInstance(); // this will raise an EntityNotFound exception
        // when id is invalid and conversation will not
        // start
    }

    private class GroupMaintainerAutocomplete extends MaintainerAutocomplete {

        @Override
        protected List<HPerson> getMaintainers() {
            return getInstanceMaintainers();
        }

        /**
         * Action when an item is selected
         */
        @Override
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }

            HPerson maintainer = personDAO.findByUsername(getSelectedItem());
            getInstance().getMaintainers().add(maintainer);
            update(conversationScopeMessages);
            reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.MaintainerAddedToGroup",
                            maintainer.getName()));
        }
    }

    private class VersionAutocomplete extends
            AbstractAutocomplete<HProjectIteration> {
        private ProjectIterationDAO projectIterationDAO = ServiceLocator
                .instance().getInstance(ProjectIterationDAO.class);

        private VersionGroupService versionGroupServiceImpl = ServiceLocator
                .instance().getInstance(VersionGroupServiceImpl.class);

        @Override
        public List<HProjectIteration> suggest() {
            List<HProjectIteration> versionList =
                    versionGroupServiceImpl
                            .searchLikeSlugOrProjectSlug(getQuery());

            Collection<HProjectIteration> filtered =
                    Collections2.filter(versionList,
                            new Predicate<HProjectIteration>() {
                                @Override
                                public boolean apply(
                                        @Nullable HProjectIteration input) {
                                    return !input.getGroups().contains(
                                            getInstance());
                                }
                            });

            return Lists.newArrayList(filtered);
        }

        @Override
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }

            HProjectIteration version =
                    projectIterationDAO.findById(new Long(getSelectedItem()));
            getInstance().getProjectIterations().add(version);
            update(conversationScopeMessages);
            reset();

            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.VersionAddedToGroup", version.getSlug(),
                            version.getProject().getSlug()));
        }
    }

    private class GroupLocaleAutocomplete extends LocaleAutocomplete {

        @Override
        protected Set<HLocale> getLocales() {
            // not used because overriding List<HLocale> suggest()
            return null;
        }

        @Override
        public List<HLocale> suggest() {
            List<HLocale> localeList = localeServiceImpl.getSupportedLocales();

            Collection<HLocale> filtered =
                    Collections2.filter(localeList, new Predicate<HLocale>() {
                        @Override
                        public boolean apply(HLocale input) {
                            return !getInstance().getActiveLocales().contains(
                                    input)
                                    && (StringUtils.startsWithIgnoreCase(input
                                            .getLocaleId().getId(), getQuery()) || StringUtils.containsIgnoreCase(
                                            input.retrieveDisplayName(),
                                            getQuery()));
                        }
                    });

            return Lists.newArrayList(filtered);
        }

        /**
         * Action when an item is selected
         */
        @Override
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }
            HLocale locale = localeServiceImpl.getByLocaleId(getSelectedItem());

            getInstance().getActiveLocales().add(locale);

            update(conversationScopeMessages);
            reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.LanguageAddedToGroup",
                            locale.retrieveDisplayName()));
        }
    }

}

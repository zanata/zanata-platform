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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.SlugEntityService;
import org.zanata.service.VersionGroupService;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.AbstractListFilter;
import org.zanata.ui.InMemoryListFilter;
import org.zanata.ui.autocomplete.LocaleAutocomplete;
import org.zanata.ui.autocomplete.MaintainerAutocomplete;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.ComparatorUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("versionGroupHome")
@ViewScoped
@Model
@Transactional
public class VersionGroupHome extends SlugHome<HIterationGroup>
        implements Serializable {
    private static final long serialVersionUID = 1L;
    @Inject
    @Any
    private VersionGroupSlug versionGroupSlug;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private SlugEntityService slugEntityServiceImpl;
    @Inject
    private Messages msgs;
    @Inject
    private ConversationScopeMessages conversationScopeMessages;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private GroupMaintainerAutocomplete maintainerAutocomplete;
    @Inject
    private VersionAutocomplete versionAutocomplete;
    @Inject
    private GroupLocaleAutocomplete localeAutocomplete;
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

    public VersionGroupHome() {
        setEntityClass(HIterationGroup.class);
    }

    public String getSlug() {
        return versionGroupSlug.getValue();
    }

    public void setSlug(String slug) {
        versionGroupSlug.setValue(slug);
    }

    public void createNew() {
        clearSlugs();
        identity.checkPermission(getInstance(), "insert");
    }

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
    @Transactional
    public String persist() {
        identity.checkPermission(getInstance(), "update");
        if (!validateSlug(getInstance().getSlug(), "slug"))
            return null;
        if (authenticatedAccount != null) {
            getInstance().addMaintainer(authenticatedAccount.getPerson());
        }
        return super.persist();
    }

    @Override
    @Transactional
    public String update() {
        identity.checkPermission(getInstance(), "update");
        String state = super.update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.get("jsf.group.settings.updated"));
        return state;
    }
    // TODO ask camunoz if this is still needed

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

    @Transactional
    public void setStatus(char initial) {
        getInstance().setStatus(EntityStatus.valueOf(initial));
    }

    @Transactional
    public void removeLanguage(HLocale locale) {
        identity.checkPermission(getInstance(), "update");
        getInstance().getActiveLocales().remove(locale);
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.LanguageRemoveFromGroup",
                        locale.retrieveDisplayName()));
    }

    @Transactional
    public void removeVersion(HProjectIteration version) {
        identity.checkPermission(getInstance(), "update");
        getInstance().getProjectIterations().remove(version);
        update();
        conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                msgs.format("jsf.VersionRemoveFromGroup", version.getSlug(),
                        version.getProject().getSlug()));
    }

    @Transactional
    public void removeMaintainer(HPerson maintainer) {
        identity.checkPermission(getInstance(), "update");
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
        Collections.sort(list, ComparatorUtil.VERSION_PROJECT_NAME_COMPARATOR);
        return list;
    }

    public List<HPerson> getInstanceMaintainers() {
        List<HPerson> list = Lists.newArrayList(getInstance().getMaintainers());
        Collections.sort(list, ComparatorUtil.PERSON_NAME_COMPARATOR);
        return list;
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", getSlug());
    }

    @Override
    public boolean isIdDefined() {
        return getSlug() != null;
    }

    @Override
    public Object getId() {
        return getSlug();
    }

    public void validateSuppliedId() {
        getInstance(); // this will raise an EntityNotFound exception
        // when id is invalid and conversation will not
        // start
        clearSlugs();
    }

    @ViewScoped
    public static class GroupMaintainerAutocomplete
            extends MaintainerAutocomplete {

        @Inject
        private VersionGroupHome versionGroupHome;
        @Inject
        private ZanataIdentity identity;
        @Inject
        private Messages msgs;

        @Override
        protected List<HPerson> getMaintainers() {
            return versionGroupHome.getInstanceMaintainers();
        }

        private HIterationGroup getInstance() {
            return versionGroupHome.getInstance();
        }

        /**
         * Action when an item is selected
         */
        @Override
        @Transactional
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }
            identity.checkPermission(getInstance(), "update");
            HPerson maintainer = personDAO.findByUsername(getSelectedItem());
            getInstance().getMaintainers().add(maintainer);
            versionGroupHome.update(conversationScopeMessages);
            reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.MaintainerAddedToGroup",
                            maintainer.getName()));
        }
    }

    @ViewScoped
    public static class VersionAutocomplete
            extends AbstractAutocomplete<HProjectIteration> {

        @Inject
        private ProjectIterationDAO projectIterationDAO;
        @Inject
        private VersionGroupService versionGroupServiceImpl;
        @Inject
        private VersionGroupHome versionGroupHome;
        @Inject
        private ZanataIdentity identity;
        @Inject
        private Messages msgs;

        private HIterationGroup getInstance() {
            return versionGroupHome.getInstance();
        }

        @Override
        public List<HProjectIteration> suggest() {
            List<HProjectIteration> versionList = versionGroupServiceImpl
                    .searchLikeSlugOrProjectSlug(getQuery());
            Collection<HProjectIteration> filtered = Collections2
                    .filter(versionList, new Predicate<HProjectIteration>() {

                        @Override
                        public boolean
                                apply(@Nullable HProjectIteration input) {
                            return input != null && !input.getGroups()
                                    .contains(getInstance());
                        }
                    });
            return Lists.newArrayList(filtered);
        }

        @Override
        @Transactional
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }
            identity.checkPermission(getInstance(), "update");
            HProjectIteration version =
                    projectIterationDAO.findById(new Long(getSelectedItem()));
            getInstance().getProjectIterations().add(version);
            versionGroupHome.update(conversationScopeMessages);
            reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.VersionAddedToGroup", version.getSlug(),
                            version.getProject().getSlug()));
        }
    }

    @ViewScoped
    public static class GroupLocaleAutocomplete extends LocaleAutocomplete {

        @Inject
        private VersionGroupHome versionGroupHome;
        @Inject
        private ZanataIdentity identity;
        @Inject
        private Messages msgs;

        private HIterationGroup getInstance() {
            return versionGroupHome.getInstance();
        }

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
                            return !getInstance().getActiveLocales()
                                    .contains(input)
                                    && (StringUtils.startsWithIgnoreCase(
                                            input.getLocaleId().getId(),
                                            getQuery())
                                            || StringUtils.containsIgnoreCase(
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
        @Transactional
        public void onSelectItemAction() {
            if (StringUtils.isEmpty(getSelectedItem())) {
                return;
            }
            identity.checkPermission(getInstance(), "update");
            HLocale locale = localeServiceImpl.getByLocaleId(getSelectedItem());
            getInstance().getActiveLocales().add(locale);
            versionGroupHome.update();
            reset();
            versionGroupHome.getMaintainerFilter().reset();
            conversationScopeMessages.setMessage(FacesMessage.SEVERITY_INFO,
                    msgs.format("jsf.LanguageAddedToGroup",
                            locale.retrieveDisplayName()));
        }
    }

    public VersionGroupSlug getVersionGroupSlug() {
        return this.versionGroupSlug;
    }

    public GroupMaintainerAutocomplete getMaintainerAutocomplete() {
        return this.maintainerAutocomplete;
    }

    public VersionAutocomplete getVersionAutocomplete() {
        return this.versionAutocomplete;
    }

    public GroupLocaleAutocomplete getLocaleAutocomplete() {
        return this.localeAutocomplete;
    }

    public AbstractListFilter<HPerson> getMaintainerFilter() {
        return this.maintainerFilter;
    }
}

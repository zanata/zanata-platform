/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.model;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Describes all the membership roles of a person in a project.
 *
 * This is designed for use in the project permissions editing dialog. It uses
 * boolean properties to represent all roles so they can be easily bound to
 * individual UI elements (e.g. checkboxes).
 */
public class PersonProjectMemberships {
    private HPerson person;
    private boolean maintainer;
    private boolean translationMaintainer;
    private Set<LocaleRoles> localeRoles;

    public PersonProjectMemberships(HPerson person,
            Collection<ProjectRole> projectRoles,
            ListMultimap<HLocale, LocaleRole> localeRoleMappings) {
        this.person = person;
        maintainer = projectRoles != null
                && projectRoles.contains(ProjectRole.Maintainer);
        translationMaintainer = projectRoles != null
                && projectRoles.contains(ProjectRole.TranslationMaintainer);
        localeRoles = Sets.newHashSet();
        if (localeRoleMappings != null) {
            for (Map.Entry<HLocale, Collection<LocaleRole>> entry : localeRoleMappings
                    .asMap().entrySet()) {
                localeRoles
                        .add(new LocaleRoles(entry.getKey(), entry.getValue()));
            }
        }
    }

    public ImmutableList<LocaleRoles> getSortedLocaleRoles() {
        return ImmutableList
                .copyOf(ImmutableSortedSet.orderedBy(LOCALE_NAME_ORDERING)
                        .addAll(getLocaleRoles()).build());
    }

    /**
     * Add a set of roles for this person in a particular locale.
     */
    public void addLocaleRoles(HLocale locale, Collection<LocaleRole> roles) {
        this.getLocaleRoles().add(new LocaleRoles(locale, roles));
    }

    /**
     * Ensure there is a representation for all of the given locales.
     *
     * This should be used to create LocaleRoles objects for locales that the
     * person does not have any membership in. Locales that are already
     * represented do not cause any change.
     *
     * @param locales
     *            all the locales that should be present, may include locales
     *            that the person is already a member of.
     */
    public void ensureLocalesPresent(Collection<HLocale> locales) {
        Collection<HLocale> presentLocales =
                Collections2.transform(localeRoles, TO_LOCALE);
        for (HLocale locale : locales) {
            if (!presentLocales.contains(locale)) {
                localeRoles.add(new LocaleRoles(locale, Collections.EMPTY_SET));
            }
        }
    }

    /**
     * Transform to extract the name of the locale from a LocaleRoles (for
     * sorting)
     *
     * Use with {@link com.google.common.collect.Collections2#transform}
     */
    public static final Function<LocaleRoles, String> TO_LOCALE_NAME =
            new Function<LocaleRoles, String>() {

                @Nullable
                @Override
                public String apply(LocaleRoles input) {
                    // To lowercase to prevent non-caps values appearing after
                    // all caps values (e.g. a appearing after Z)
                    return input.getLocale().retrieveDisplayName()
                            .toLowerCase();
                }
            };
    private static final Ordering<LocaleRoles> LOCALE_NAME_ORDERING =
            Ordering.natural().onResultOf(TO_LOCALE_NAME);

    /**
     * Transform to extract the locale from a LocaleRoles.
     *
     * Use with {@link com.google.common.collect.Collections2#transform}
     */
    public static final Function<LocaleRoles, HLocale> TO_LOCALE =
            new Function<LocaleRoles, HLocale>() {

                @Nullable
                @Override
                public HLocale apply(LocaleRoles input) {
                    return input.getLocale();
                }
            };

    /**
     * Replace the person with an equivalent person.
     *
     * Hibernate can fail in persistence if the person object is 'detached' so
     * this allows replacement with an equivalent 'attached' version.
     *
     * The person must be equal to the existing person according to .equals() or
     * an exception will be thrown.
     *
     * @param person
     *            to set, must be equal to the current person
     */
    public void setPerson(HPerson person) {
        if (person.equals(getPerson())) {
            this.person = person;
        } else {
            throw new IllegalArgumentException(
                    "Cannot set a different person. This is only for replacing a detached HPerson with anequivalent attached HPerson (according to .equals)");
        }
    }

    /**
     * @return false if there are no selected membership permissions, otherwise
     *         true
     */
    public boolean hasAnyPermissions() {
        return maintainer || translationMaintainer || hasAnyLocalePermissions();
    }

    /**
     * @return false if there are no selected roles for any locale, otherwise
     *         true
     */
    private boolean hasAnyLocalePermissions() {
        for (LocaleRoles roles : localeRoles) {
            if (roles.isTranslator() || roles.isReviewer()
                    || roles.isCoordinator() || roles.isGlossarist()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Represents a locale and the membership in each locale role.
     *
     * Intended to use as a row for a single locale in a permission setting
     * table.
     */
    public class LocaleRoles {
        private HLocale locale;
        private boolean translator;
        private boolean reviewer;
        private boolean coordinator;
        private boolean glossarist;

        public LocaleRoles(HLocale locale, Collection<LocaleRole> roles) {
            this.locale = locale;
            translator = roles.contains(LocaleRole.Translator);
            reviewer = roles.contains(LocaleRole.Reviewer);
            coordinator = roles.contains(LocaleRole.Coordinator);
            glossarist = roles.contains(LocaleRole.Glossarist);
        }

        /**
         * Set the locale to an equal locale object.
         *
         * This is to work around Hibernate, which fails when trying to persist
         * something with a 'detached' HLocale in it.
         *
         * This is to allow replacement of a 'detached' HLocale entity with an
         * 'attached' HLocale entity. It is only intended to set an attached
         * version of the same locale, and will throw an exception if a
         * different locale is set.
         *
         * @param locale
         *            to set, must be equal to the current locale.
         */

        public void setLocale(HLocale locale) {
            if (locale.equals(getLocale())) {
                this.locale = locale;
            } else {
                throw new IllegalArgumentException(
                        "Cannot set to a different locale. This is only for replacing a detached HLocale with an equivalent attached HLocale (according to .equals)");
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (!(obj instanceof LocaleRoles)) {
                return false;
            } else {
                final LocaleRoles other = (LocaleRoles) obj;
                return getLocale().equals(other.getLocale());
            }
        }

        @Override
        public int hashCode() {
            return locale.hashCode();
        }

        public void setTranslator(final boolean translator) {
            this.translator = translator;
        }

        public void setReviewer(final boolean reviewer) {
            this.reviewer = reviewer;
        }

        public void setCoordinator(final boolean coordinator) {
            this.coordinator = coordinator;
        }

        public void setGlossarist(final boolean glossarist) {
            this.glossarist = glossarist;
        }

        public HLocale getLocale() {
            return this.locale;
        }

        public boolean isTranslator() {
            return this.translator;
        }

        public boolean isReviewer() {
            return this.reviewer;
        }

        public boolean isCoordinator() {
            return this.coordinator;
        }

        public boolean isGlossarist() {
            return this.glossarist;
        }
    }

    public HPerson getPerson() {
        return this.person;
    }

    public boolean isMaintainer() {
        return this.maintainer;
    }

    public boolean isTranslationMaintainer() {
        return this.translationMaintainer;
    }

    public Set<LocaleRoles> getLocaleRoles() {
        return this.localeRoles;
    }

    public void setMaintainer(final boolean maintainer) {
        this.maintainer = maintainer;
    }

    public void setTranslationMaintainer(final boolean translationMaintainer) {
        this.translationMaintainer = translationMaintainer;
    }
}

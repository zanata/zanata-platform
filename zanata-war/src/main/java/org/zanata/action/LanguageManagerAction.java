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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.faces.model.SelectItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.service.LocaleService;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.FilterUtil;
import org.zanata.ui.autocomplete.LocaleAutocomplete;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.ibm.icu.util.ULocale;

@Named("languageManagerAction")
@javax.faces.bean.ViewScoped

public class LanguageManagerAction extends AbstractAutocomplete<HLocale>
        implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int LENGTH_LIMIT = 254;

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private LocaleService localeServiceImpl;

    @Inject
    private ResourceUtils resourceUtils;

    @Inject
    private Messages msgs;

    @Getter
    @Setter
    private ULocale uLocale;

    @Getter
    @Setter
    private boolean enabledByDefault = true;

    // cache this so it is called only once
    private List<LocaleId> allLocales;

    @Getter
    private String languageNameValidationMessage;

    @Getter
    private String languageNameWarningMessage;

    @PostConstruct
    public void onCreate() {
        allLocales = localeServiceImpl.getAllJavaLanguages();
    }

    public void updateLanguage(String language) {
        if (!StringUtils.isEmpty(language)) {
            uLocale = new ULocale(language);
            setQuery(language);
            isLanguageNameValid();
        } else {
            uLocale = null;
        }
    }

    @CheckRole("admin")
    @Transactional
    public String save() {
        if (!isLanguageNameValid()) {
            return null; // not success
        }
        LocaleId locale = new LocaleId(getQuery());
        localeServiceImpl.save(locale, enabledByDefault);
        return "success";
    }

    public boolean isLanguageNameValid() {
        languageNameValidationMessage = null; // reset
        languageNameWarningMessage = null; // reset

        if (StringUtils.isEmpty(getQuery()) || getQuery().length() > LENGTH_LIMIT) {
            uLocale = null;
            languageNameValidationMessage =
                    msgs.get("jsf.language.validation.Invalid");
            return false;
        }

        // Cannot use FacesMessages as they are request scoped.
        // Cannot use UI binding as they don't work in Page scoped beans
        // TODO Use the new (since 1.7) FlashScopeBean

        // Check that locale Id is syntactically valid
        LocaleId localeId;
        try {
            localeId = new LocaleId(getQuery());
        } catch (IllegalArgumentException iaex) {
            languageNameValidationMessage =
                    msgs.get("jsf.language.validation.Invalid");
            return false;
        }

        // check for already registered languages
        if (localeServiceImpl.localeExists(localeId)) {
            languageNameValidationMessage =
                    msgs.get("jsf.language.validation.Existing");
            return false;
        }

        // Check for plural forms
        if (resourceUtils.getPluralForms(localeId, true, false) == null) {
            languageNameWarningMessage =
                    msgs.get("jsf.language.validation.UnknownPluralForm");
        }

        // Check for similar already registered languages (warning)
        List<HLocale> similarLangs = localeDAO.findBySimilarLocaleId(localeId);
        if (similarLangs.size() > 0) {
            languageNameWarningMessage =
                    msgs.get("jsf.language.validation.SimilarLocaleFound")
                            + similarLangs.get(0).getLocaleId().getId();
        }
        return true;
    }

    @Override
    public List<HLocale> suggest() {
        if (StringUtils.isEmpty(getQuery())) {
            return Collections.EMPTY_LIST;
        }

        Collection<HLocale> locales = Collections2.transform(allLocales,
                new Function<LocaleId, HLocale>() {
                    @Override
                    public HLocale apply(@Nullable LocaleId from) {
                        return new HLocale(from);
                    }
                });

        Collection<HLocale> filtered =
                Collections2.filter(locales, new Predicate<HLocale>() {
                    @Override
                    public boolean apply(HLocale input) {
                        return StringUtils.containsIgnoreCase(input
                                .getLocaleId().getId(), getQuery());
                    }
                });

        if(filtered.isEmpty()) {
            updateLanguage(getQuery());
        }
        return Lists.newArrayList(filtered);
    }

    @Override
    public void onSelectItemAction() {
        if (StringUtils.isEmpty(getSelectedItem())) {
            return;
        }
        updateLanguage(getSelectedItem());
    }

    public void replaceUnderscore(String language) {
        setQuery(language);
        updateLanguage(language);
    }

    public void resetValue() {
        setQuery("");
        uLocale = null;
        languageNameValidationMessage = null; // reset
        languageNameWarningMessage = null; // reset
    }
}

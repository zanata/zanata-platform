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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.ibm.icu.util.ULocale;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HLocale;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.service.LocaleService;

import javax.annotation.Nullable;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Name("languageManagerAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
public class LanguageManagerAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private LocaleDAO localeDAO;

    @In
    private LocaleService localeServiceImpl;

    @In
    private ResourceUtils resourceUtils;

    @In
    private Map<String, String> messages;

    private String language;

    private ULocale uLocale;

    private List<SelectItem> localeStringList;

    private boolean enabledByDefault = true;

    // cache this so it is called only once
    private List<LocaleId> allLocales;

    private String languageNameValidationMessage;

    private String languageNameWarningMessage;

    @Create
    public void onCreate() {
        fectchLocaleFromJava();
    }

    public String getLanguage() {
        return language;
    }

    public ULocale getuLocale() {
        return uLocale;
    }

    public void setuLocale(ULocale uLocale) {
        this.uLocale = uLocale;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public void setEnabledByDefault(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }

    public String getLanguageNameValidationMessage() {
        return languageNameValidationMessage;
    }

    public String getLanguageNameWarningMessage() {
        return languageNameWarningMessage;
    }

    public void updateLanguage() {
        if (this.language.trim().length() > 0) {
            this.uLocale = new ULocale(this.language);
            this.isLanguageNameValid();
        } else {
            this.uLocale = null;
        }
    }

    public String save() {
        if (!isLanguageNameValid()) {
            return null; // not success
        }
        LocaleId locale = new LocaleId(language);
        localeServiceImpl.save(locale, enabledByDefault);
        return "success";
    }

    public void fectchLocaleFromJava() {
        List<LocaleId> locale = localeServiceImpl.getAllJavaLanguages();
        List<SelectItem> localeList = new ArrayList<SelectItem>();
        for (LocaleId var : locale) {
            SelectItem op = new SelectItem(var.getId(), var.getId());
            localeList.add(op);
        }
        localeStringList = localeList;
    }

    public List<SelectItem> getLocaleStringList() {
        return localeStringList;
    }

    public List<HLocale> suggestLocales(final String query) {
        if (allLocales == null) {
            allLocales = localeServiceImpl.getAllJavaLanguages();
        }

        Collection<LocaleId> filtered =
                Collections2.filter(allLocales, new Predicate<LocaleId>() {
                    @Override
                    public boolean apply(@Nullable LocaleId input) {
                        return input.getId().startsWith(query);
                    }
                });

        return new ArrayList<HLocale>(Collections2.transform(filtered,
                new Function<LocaleId, HLocale>() {
                    @Override
                    public HLocale apply(@Nullable LocaleId from) {
                        return new HLocale(from);
                    }
                }));
    }

    public boolean isLanguageNameValid() {
        this.languageNameValidationMessage = null; // reset
        this.languageNameWarningMessage = null; // reset

        // Cannot use FacesMessages as they are request scoped.
        // Cannot use UI binding as they don't work in Page scoped beans
        // TODO Use the new (since 1.7) FlashScopeBean

        // Check that locale Id is syntactically valid
        LocaleId localeId;
        try {
            localeId = new LocaleId(language);
        } catch (IllegalArgumentException iaex) {
            this.languageNameValidationMessage =
                    messages.get("jsf.language.validation.Invalid");
            return false;
        }

        // check for already registered languages
        if (localeServiceImpl.localeExists(localeId)) {
            this.languageNameValidationMessage =
                    messages.get("jsf.language.validation.Existing");
            return false;
        }

        // Check for plural forms
        if (resourceUtils.getPluralForms(localeId, false) == null) {
            this.languageNameWarningMessage =
                    messages.get("jsf.language.validation.UnknownPluralForm");
        }

        // Check for similar already registered languages (warning)
        List<HLocale> similarLangs = localeDAO.findBySimilarLocaleId(localeId);
        if (similarLangs.size() > 0) {
            this.languageNameWarningMessage =
                    messages.get("jsf.language.validation.SimilarLocaleFound")
                            + similarLangs.get(0).getLocaleId().getId();
        }

        return true;
    }

}

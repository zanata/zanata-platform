/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.List;
import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;

/**
 * Describes all operations necessary to manipulate language list settings.
 *
 * This provides a compile-time check that an implementing class will work with
 * JSF templates for language settings. It should be kept up-to-date with all
 * the properties and actions used by the JSF templates.
 *
 * The templates are:
 *
 *  - WEB-INF/layout/project/settings-tab-languages.xhtml
 */
public interface HasLanguageSettings {

    /**
     * Whether to override the default locales inherited from the parent (server
     * or project) with a custom list of locales.
     */
    boolean isOverrideLocales();
    void setOverrideLocales(boolean overrideLocales);

    Map<LocaleId, String> getLocaleAliases();

    void removeAllLocaleAliases();

    /**
     * Remove locale aliases from any enabled locale that is selected.
     *
     * An enabled locale is selected if it maps to true in
     * selectedEnabledLocales.
     */
    void removeSelectedLocaleAliases();

    String getLocaleAlias(HLocale locale);
    boolean hasLocaleAlias(HLocale locale);

    /**
     * Locale aliases that have been entered by the user, but may not yet have
     * been saved.
     */
    Map<LocaleId, String> getEnteredLocaleAliases();
    void setEnteredLocaleAliases(Map<LocaleId, String> enteredLocaleAliases);

    /**
     * Update the locale alias for a given locale to use the value entered by
     * the user.
     *
     * The entered value is looked up in enteredLocaleAliases using the supplied
     * LocaleId as the key.
     */
    void updateToEnteredLocaleAlias(LocaleId localeId);

    /**
     * Ensure that only all the default locales are active.
     *
     * After this has run, overrideLocales will return false.
     *
     * This will remove locale aliases from any locales that are deactivated.
     */
    void useDefaultLocales();

    /**
     * Filter text that is being used to determine which enabled locales are
     * visible in the user interface.
     */
    String getEnabledLocalesFilter();
    void setEnabledLocalesFilter(String enabledLocalesFilter);

    List<HLocale> getEnabledLocales();

    /**
     * Enabled locales that have a selected state in the user interface.
     */
    Map<LocaleId, Boolean> getSelectedEnabledLocales();
    void setSelectedEnabledLocales(Map<LocaleId, Boolean> selectedEnabledLocales);

    /**
     * Disable any enabled locale that is selected.
     *
     * An enabled locale is selected if its localeId maps to true in
     * selectedEnabledLocales.
     */
    void disableSelectedLocales();

    void disableLocale(HLocale locale);

    /**
     * Filter text that is being used to determine which disabled locales are
     * visible in the user interface.
     */
    String getDisabledLocalesFilter();
    void setDisabledLocalesFilter(String disabledLocalesFilter);

    List<HLocale> getDisabledLocales();

    Map<LocaleId, Boolean> getSelectedDisabledLocales();
    void setSelectedDisabledLocales(Map<LocaleId, Boolean> selectedDisabledLocales);

    /**
     * Enable any disabled locale that is selected.
     *
     * A disabled locale is selected if its localeId maps to true in
     * selectedDisabledLocales.
     */
    void enableSelectedLocales();

    void enableLocale(HLocale locale);
}

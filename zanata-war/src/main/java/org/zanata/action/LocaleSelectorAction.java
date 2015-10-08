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
package org.zanata.action;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.zanata.events.LocaleSelectedEvent;
import org.zanata.servlet.HttpRequestAndSessionHolder;
import org.zanata.util.Event;
import org.zanata.util.ServiceLocator;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("localeSelectorAction")
@Scope(ScopeType.SESSION)
public class LocaleSelectorAction {

    private String language;
    private String country;
    private String variant;

    public String getLocaleString() {
        return getLocale().toString();
    }

    public void select(ValueChangeEvent event) {
        setLocaleString((String) event.getNewValue());
        select();
    }

    /**
     * Force the resource bundle to reload, using the current locale,
     * and raise the org.zanata.events.LocaleSelectedEvent event.
     */
    public void select() {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(getLocale());

        getLocaleSelectedEvent().fire(
                new LocaleSelectedEvent(getLocaleString()));
    }

    private Event<LocaleSelectedEvent> getLocaleSelectedEvent() {
        return ServiceLocator.instance().getInstance(Event.class);
    }


    public void setLocaleString(String localeString) {
        StringTokenizer tokens = new StringTokenizer(localeString, "-_");
        language = tokens.hasMoreTokens() ? tokens.nextToken() : null;
        country =  tokens.hasMoreTokens() ? tokens.nextToken() : null;
        variant =  tokens.hasMoreTokens() ? tokens.nextToken() : null;
    }

    /**
     * Get the selected locale
     */
    public Locale getLocale() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            Locale jsfLocale = facesContext.getApplication().getViewHandler()
                    .calculateLocale(facesContext);
            if (Contexts.isSessionContextActive()) {
                return calculateLocale(jsfLocale);
            }
        }

        Optional<HttpServletRequest> requestOpt =
                HttpRequestAndSessionHolder.getRequest();
        if (requestOpt.isPresent()) {
            ServletRequest request = requestOpt.get();
            if (request != null) {
                return calculateLocale(request.getLocale());
            }
        }

        return calculateLocale(Locale.getDefault());
    }

    private Locale calculateLocale(Locale jsfLocale) {
        if (!Strings.isNullOrEmpty(variant)) {
            return new java.util.Locale(language, country, variant);
        } else if (!Strings.isNullOrEmpty(country)) {
            return new java.util.Locale(language, country);
        } else if (!Strings.isNullOrEmpty(language)) {
            return new java.util.Locale(language);
        } else {
            return jsfLocale;
        }
    }

    public List<SelectItem> getSupportedLocales() {
        Iterator<Locale> locales = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
        List<SelectItem> selectItems =
                Lists.transform(Lists.newArrayList(locales),
                        new Function<Locale, SelectItem>() {
                            @Override
                            public SelectItem apply(Locale locale) {
                                return new SelectItem(locale.toString(),
                                        locale.getDisplayName(locale));
                            }
                        });
        return selectItems;
    }

    public void setLocale(Locale locale) {
        language = Strings.emptyToNull(locale.getLanguage());
        country = Strings.emptyToNull(locale.getCountry());
        variant = Strings.emptyToNull(locale.getVariant());
    }
}

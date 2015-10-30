/*
 * Copyright 2010-2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.i18n;

import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Named;

import org.zanata.action.LocaleSelectorAction;
import org.zanata.events.LocaleSelectedEvent;
import org.zanata.util.Contexts;
import org.zanata.util.EmptyEnumeration;
import org.zanata.util.ServiceLocator;

import javax.annotation.Nonnull;
import javax.enterprise.event.Observes;

/**
 * Utility component to help with programmatic access to the message resource
 * bundle.
 *
 * Unlike the {@link org.jboss.seam.international.Messages} component, this
 * component formats messages using positional arguments like {0} and
 * {1}, not by interpolating EL expressions.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

@Named("msgs")
@javax.enterprise.context.RequestScoped
public class Messages extends AbstractMap<String, String> {

    /**
     * Gets the locale of the current request, if any, otherwise server's
     * default locale.
     */
    private static Locale getLocale() {
        // TODO [CDI] inject LocaleResolver but make sure it handles session locale
        if (Contexts.isSessionContextActive()) {
            LocaleSelectorAction selectorAction = ServiceLocator.instance()
                    .getInstance(LocaleSelectorAction.class);
            return selectorAction.getLocale();
        }
        return Locale.getDefault();
    }

    /**
     * Gets the 'messages' ResourceBundle for the specified locale.
     */
    private static ResourceBundle getResourceBundle(Locale locale) {
        // Generic ResourceBundle without built-in interpolation:
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle(
                    "messages", locale);
        } catch (MissingResourceException e) {
            resourceBundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(@Nonnull String key) {
                    return key;
                }

                @Override
                @Nonnull
                public Enumeration<String> getKeys() {
                    return EmptyEnumeration.instance();
                }
            };
        }
        return resourceBundle;
    }

    /**
     * Currently selected locale (observes LocaleSelectedEvent)
     */
    private Locale locale;
    private transient ResourceBundle resourceBundle;

    /**
     * Create an instance for the locale of the current request, if any,
     * otherwise the server's default locale.
     */
    public Messages() {
        this(getLocale());
    }

    /**
     * Create an instance for the specified locale.
     */
    public Messages(Locale locale) {
        this.locale = locale;
        this.resourceBundle = getResourceBundle(locale);
    }

    private ResourceBundle getBundle() {
        if (resourceBundle != null) {
            resourceBundle = getResourceBundle(locale);
        }
        return resourceBundle;
    }

    public void changeLocale(@Observes LocaleSelectedEvent event) {
        // we need to refresh the bean
        // see org.jboss.seam.international.LocaleSelector.select()
        this.locale = event.getLocale();
        this.resourceBundle = null;
    }

    // the default toString includes the entire list of properties,
    // which makes a mess of the log file
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Gets a resource string, without any message formatting.  (So an
     * apostrophe just represents an apostrophe (single quote).)
     * @param key ResourceBundle key
     * @return ResourceBundle string
     */
    @Override
    public String get(Object key) {
        if (key instanceof String) {
            String resourceKey = (String) key;
            try {
                return getBundle().getString(resourceKey);
            } catch (MissingResourceException mre) {
                return resourceKey;
            }
        } else {
            return null;
        }
    }

    /**
     * @deprecated use get(key) or format(key, args...)
     * @param key ResourceBundle key to look up the format string
     * @return formatted string
     */
    @Deprecated
    public String format(String key) {
        return formatWithAnyArgs(key, new Object[0]);
    }

    /**
     * Gets a resource string, and formats it using MessageFormat and the
     * positional parameters.  Due to the use of {@link MessageFormat}
     * any literal apostrophes (single quotes) will need to be doubled,
     * otherwise they will be interpreted as quoting format patterns.
     * @param key ResourceBundle key to look up the format string
     * @param args arguments for interpolation by MessageFormat
     * @return formatted string
     * @see MessageFormat
     */
    public String formatWithAnyArgs(String key, Object... args) {
        String template = get(key);
        return MessageFormat.format(template, args);
    }

    // JSF can't handle varargs, hence the need for these overloaded methods:
    public String format(String key, Object arg1) {
        return formatWithAnyArgs(key, arg1);
    }

    public String format(String key, Object arg1, Object arg2) {
        return formatWithAnyArgs(key, arg1, arg2);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3) {
        return formatWithAnyArgs(key, arg1, arg2, arg3);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
        return formatWithAnyArgs(key, arg1, arg2, arg3, arg4);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return formatWithAnyArgs(key,
                arg1, arg2, arg3, arg4, arg5);
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        return formatWithAnyArgs(key,
                arg1, arg2, arg3, arg4, arg5, arg6);
    }

    @Nonnull
    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> entrySet =
                new HashSet<Entry<String, String>>();

        for (final String key : getBundle().keySet()) {
            entrySet.add(new Map.Entry<String, String>() {

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public String getValue() {
                    return get(key);
                }

                @Override
                public String setValue(String val) {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return entrySet;
    }
}

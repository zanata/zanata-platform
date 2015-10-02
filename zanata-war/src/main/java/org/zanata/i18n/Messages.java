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
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Named;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.zanata.util.EmptyEnumeration;

import javax.annotation.Nonnull;

import static org.jboss.seam.ScopeType.EVENT;

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
     * Gets the 'messages' ResourceBundle for the locale of the current
     * request, if any, otherwise server's default locale.
     * @see org.jboss.seam.web.Locale
     */
    private static ResourceBundle getResourceBundle() {
        return getResourceBundle(org.jboss.seam.core.Locale.instance());
    }

    /**
     * Gets the 'messages' ResourceBundle for the specified locale.
     */
    private static ResourceBundle getResourceBundle(java.util.Locale locale) {
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

    private final ResourceBundle resourceBundle;

    /**
     * Create an instance for the locale of the current request, if any,
     * otherwise the server's default locale.
     */
    public Messages() {
        this(getResourceBundle());
    }

    /**
     * Create an instance for the specified locale.
     */
    public Messages(java.util.Locale locale) {
        this(getResourceBundle(locale));
    }

    private Messages(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Observer("org.jboss.seam.localeSelected")
    public void changeLocale(String localeString) {
        // we need to refresh the bean
        // see org.jboss.seam.international.LocaleSelector.select()
        Contexts.removeFromAllContexts("msgs");
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
     * @param key
     * @return
     */
    @Override
    public String get(Object key) {
        if (key instanceof String) {
            String resourceKey = (String) key;
            try {
                String resource = resourceBundle.getString(resourceKey);
                return (resource == null) ? resourceKey : resource;
            } catch (MissingResourceException mre) {
                return resourceKey;
            }
        } else {
            return null;
        }
    }

    /**
     * @deprecated use get(key) or format(key, args...)
     * @param key
     * @return
     */
    @Deprecated
    public String format(String key) {
        return format(key, new Object[0]);
    }

    /**
     * Gets a resource string, and formats it using MessageFormat and the
     * positional parameters.  Due to the use of {@link java.util.MessageFormat}
     * any literal apostrophes (single quotes) will need to be doubled,
     * otherwise they will be interpreted as quoting format patterns.
     * @param key
     * @param args
     * @return
     * @see java.util.MessageFormat
     */
    public String format(String key, Object... args) {
        String template = get(key);
        return MessageFormat.format(template, args);
    }

    // JSF can't handle varargs, hence the need for these overloaded methods:
    public String format(String key, Object arg1) {
        return format(key, new Object[] {arg1});
    }

    public String format(String key, Object arg1, Object arg2) {
        return format(key, new Object[] {arg1, arg2});
    }

    public String format(String key, Object arg1, Object arg2, Object arg3) {
        return format(key, new Object[] {arg1, arg2, arg3});
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
        return format(key, new Object[] {arg1, arg2, arg3, arg4});
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return format(key, new Object[] {arg1, arg2, arg3, arg4, arg5});
    }

    public String format(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        return format(key, new Object[] {arg1, arg2, arg3, arg4, arg5, arg6});
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> entrySet =
                new HashSet<Entry<String, String>>();

        for (final String key : resourceBundle.keySet()) {
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

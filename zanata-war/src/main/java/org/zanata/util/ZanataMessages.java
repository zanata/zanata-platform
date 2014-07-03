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
package org.zanata.util;

import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import static org.jboss.seam.ScopeType.EVENT;
import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 * Utility component to help with programmatic access to the message resource
 * bundle.
 *
 * Unlike the {@link org.jboss.seam.international.Messages} component, this
 * component can also format messages which use position arguments like {0} and
 * {1}.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataMessages extends AbstractMap<String, String> {

    @AutoCreate
    @BypassInterceptors
    @Name("org.jboss.seam.international.messagesFactory")
    @Install(precedence = APPLICATION)
    @Scope(ScopeType.STATELESS)
    public static class Factory {
        @org.jboss.seam.annotations.Factory(
                value = "org.jboss.seam.international.messages",
                autoCreate = true, scope = EVENT)
        public ZanataMessages getMessages() {
            return new ZanataMessages();
        }
        // TODO remove alias zanataMessages (messages is preferred)
        @org.jboss.seam.annotations.Factory(
                value = "zanataMessages",
                autoCreate = true, scope = EVENT)
        public ZanataMessages getZanataMessages() {
            return getMessages();
        }
    }

    private static ResourceBundle getResourceBundle() {
        // Generic ResourceBundle without built-in interpolation:
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle(
                    "messages", org.jboss.seam.core.Locale.instance());
        } catch (MissingResourceException e) {
            resourceBundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key) {
                    return key;
                }

                @Override
                public Enumeration<String> getKeys() {
                    return Collections.emptyEnumeration();
                }
            };
        }
        return resourceBundle;
    }

    private final ResourceBundle resourceBundle;

    public ZanataMessages() {
        this.resourceBundle = getResourceBundle();
    }

    // the default toString includes the entire list of properties,
    // which makes a mess of the log file
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Gets a resource string.  In future, this method will return a string
     * without any interpolation or message formatting.
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

    // use messages.get()
    @Deprecated
    public String getMessage(String key) {
        return get(key);
    }

    // use messages.format(), not zanataMessages.getMessage()
    @Deprecated
    public String getMessage(String key, Object... args) {
        return format(key, args);
    }

    /**
     * Gets a resource string, and interpolates both Seam context variables
     * and positional parameters.  In future, positional parameters only.
     * @param key
     * @param args
     * @return
     */
    public String format(String key, Object... args) {
        String template = get(key);
        return MessageFormat.format(template, args);
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

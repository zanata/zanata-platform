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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Role within a project that is bound to a single locale.
 */
public enum LocaleRole {

    /**
     * Coordinator for a single locale. Able to add, remove and edit roles for
     * members of the locale, and can perform all translation and review
     * operations for the locale.
     */
    Coordinator('C'),

    /**
     * Can perform all translation and review operations for the locale.
     */
    Reviewer('R'),

    /**
     * Can perform all translation operations for the locale.
     */
    Translator('T');

    private char initial;

    private static Map<Character, LocaleRole> roles = Maps.newHashMap();

    /**
     * @param initial used to represent the role in the database, see
     *                {@link org.zanata.model.type.LocaleRoleDescriptor}
     */
    LocaleRole(char initial) {
        this.initial = initial;
    }

    static {
        for (LocaleRole role : LocaleRole.values()) {
            roles.put(role.getInitial(), role);
        }
    }

    public char getInitial() {
        return initial;
    }

    public static LocaleRole valueOf(char initial) {
        LocaleRole role = roles.get(initial);
        if (role != null) {
            return role;
        }
        throw new IllegalArgumentException(
                "No locale role has an initial matching " + initial);
    }
}

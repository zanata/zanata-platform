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
 * Role within a project that is not bound to a locale.
 */
public enum ProjectRole {
    /**
     * Maintainer of the project, with full permissions to manage all aspects of
     * the project and versions, documents, teams and translations.
     */
    Maintainer('M'),

    /**
     * The maintainer for the translation team of the project, able to manage
     * all translation-related roles and perform all translation operations for
     * any locale.
     */
    TranslationMaintainer('T');

    private char initial;

    private static Map<Character, ProjectRole> roles = Maps.newHashMap();

    /**
     * @param initial used to represent the role in the database, see
     *                {@link org.zanata.model.type.ProjectRoleDescriptor}
     */
    ProjectRole(char initial) {
        this.initial = initial;
    }

    static {
        for (ProjectRole role : ProjectRole.values()) {
            roles.put(role.getInitial(), role);
        }
    }

    public char getInitial() {
        return initial;
    }

    public static ProjectRole valueOf(char initial) {
        ProjectRole role = roles.get(initial);
        if (role != null) {
            return role;
        }
        throw new IllegalArgumentException(
                "No role has an initial matching " + initial);
    }
}

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
package org.zanata.model.type;

import org.zanata.model.ProjectRole;

/**
 * Convert between a single character and a ProjectRole, used by
 * {@link org.zanata.model.type.ProjectRoleType}.
 */
public class ProjectRoleDescriptor
        extends SingleCharEnumTypeDescriptor<ProjectRole> {

    public static final ProjectRoleDescriptor INSTANCE =
            new ProjectRoleDescriptor();

    protected ProjectRoleDescriptor() {
        super(ProjectRole.class);
    }

    @Override
    char getIdentifyingChar(ProjectRole value) {
        return value.getInitial();
    }

    @Override
    ProjectRole valueOf(String string) {
        if (string.length() == 1) {
            return ProjectRole.valueOf(string.charAt(0));
        }
        throw new IllegalArgumentException(
            "String to look up a ProjectRole must be exactly 1 character. " +
            "Received \"" + string + "\"");
    }
}

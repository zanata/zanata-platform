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

package org.zanata.model.type;

import org.zanata.model.HAccountRole;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class RoleTypeDescriptor extends
    SingleCharEnumTypeDescriptor<HAccountRole.RoleType> {

    public static final RoleTypeDescriptor INSTANCE =
        new RoleTypeDescriptor();
    private static final long serialVersionUID = 6619121097493569360L;


    protected RoleTypeDescriptor() {
        super(HAccountRole.RoleType.class);
    }

    @Override
    char getIdentifyingChar(HAccountRole.RoleType value) {
        return value.getInitial();
    }

    @Override
    HAccountRole.RoleType valueOf(String string) {
        return HAccountRole.RoleType.valueOf(string.charAt(0));
    }
}

/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.security.permission;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class encupasulates calls to Seam's permission resolver chain when there
 * are multiple targets. Instances of this class will be expanded in the
 * {@link org.zanata.security.permission.CustomPermissionResolver} if it's in
 * the resolver chain. This class should not be used directly.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class MultiTargetList extends ArrayList<Object> {

    public static MultiTargetList fromTargets(Object ... targets) {
        MultiTargetList newInstance = new MultiTargetList();
        newInstance.addAll(Arrays.asList(targets));
        return newInstance;
    }
}

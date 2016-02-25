/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.Method;

import static org.zanata.security.permission.PermissionEvaluator.ALL_ACTION_GRANTER;

/**
 * A base class for all beans which may wish to provide granting methods.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class PermissionProvider {

    public final Multimap<String, PermissionGranter> getPermissionGranters() {
        Class<? extends PermissionProvider> clazz = getClass();
        Multimap<String, PermissionGranter> permissionGrantMethods =
                ArrayListMultimap.create();

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(GrantsPermission.class)) {
                PermissionGranter granter = new PermissionGranter(m);
                granter.validate();

                if (granter.getEvaluatedActions().size() == 0) {
                    // This granter is to apply to every action
                    permissionGrantMethods.put(ALL_ACTION_GRANTER, granter);
                } else {
                    for (String action : granter.getEvaluatedActions()) {
                        permissionGrantMethods.put(action, granter);
                    }
                }
            }
        }
        return permissionGrantMethods;
    }

}

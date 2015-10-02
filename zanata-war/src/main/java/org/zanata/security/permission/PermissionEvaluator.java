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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.ArrayUtils;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.jboss.seam.annotations.Startup;
import org.zanata.security.SecurityFunctions;

import com.google.common.collect.Lists;

/**
 * Holds all application permissions and provides a way to evaluate these
 * permissions for an object and an action.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("permissions")

@javax.enterprise.context.ApplicationScoped
/* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */
public class PermissionEvaluator {

    private static final String ALL_ACTION_GRANTER = "__**__";

    private final Multimap<String, PermissionGranter> permissionGrantMethods =
            ArrayListMultimap.create();

    @PostConstruct
    public void buildIndex() {
        registerPermissionGranters(SecurityFunctions.class);
    }

    /**
     * Registers all permission granter methods found in clazz to be used to
     * check permissions.
     *
     * @param clazz
     */
    public void registerPermissionGranters(Class<?> clazz) {
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
    }

    /**
     * Checks a permission for a given action on a set of targets.
     *
     * @param action
     *            The action to perform.
     * @param targets
     *            The target object instances to perform the action on.
     * @return True, if the permission has been granted. False otherwise.
     */
    public boolean checkPermission(String action, Object... targets) {
        boolean permissionGranted = false;
        // Get granters for all actions (those with no declared action)
        Collection<PermissionGranter> allActionGranters =
                permissionGrantMethods.get(ALL_ACTION_GRANTER);
        for (PermissionGranter granter : allActionGranters) {
            if (granter.shouldInvokeGranter(targets)) {
                if (granter.invoke(action, targets)) {
                    return true;
                }
            }
        }

        // Get granters for specific actions
        Collection<PermissionGranter> actionGranters =
                permissionGrantMethods.get(action);
        for (PermissionGranter granter : actionGranters) {
            if (granter.shouldInvokeGranter(targets)) {
                if (granter.invoke(action, targets)) {
                    return true;
                }
            }
        }
        return false;
    }
}

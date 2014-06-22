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

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.permission.PermissionResolver;
import org.zanata.util.ServiceLocator;

/**
 * This permission resolver will use the
 * {@link org.zanata.security.permission.PermissionEvaluator} component to
 * resolve permissions using java methods annotated with
 * {@link GrantsPermission}.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("customPermissionResolver")
@Scope(APPLICATION)
@BypassInterceptors
@Install(precedence = BUILT_IN)
@Startup
public class CustomPermissionResolver implements PermissionResolver,
        Serializable {

    @Override
    public boolean hasPermission(Object target, String action) {
        Object targetArray;
        if (target instanceof MultiTargetList) {
            targetArray = ((MultiTargetList) target).toArray();
        }
        else {
            targetArray = new Object[] { target };
        }

        return hasPermission(action, targetArray);
    }

    private boolean hasPermission(String action, Object... targets) {
        PermissionEvaluator evaluator =
                ServiceLocator.instance()
                        .getInstance(PermissionEvaluator.class);
        return evaluator.checkPermission(action, targets);
    }

    @Override
    public void filterSetByAction(Set<Object> targets, String action) {
        Iterator iter = targets.iterator();
        while (iter.hasNext()) {
            Object target = iter.next();
            if (hasPermission(target, action))
                iter.remove();
        }
    }
}

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


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This permission resolver will use the
 * {@link org.zanata.security.permission.PermissionEvaluator} component to
 * resolve permissions using java methods annotated with
 * {@link GrantsPermission}.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("customPermissionResolver")
@javax.enterprise.context.ApplicationScoped
public class CustomPermissionResolver implements Serializable {

    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "CDI proxies are Serializable")
    private PermissionEvaluator evaluator;

    private static final long serialVersionUID = 6302681723997573877L;

    public boolean hasPermission(Object target, String action) {
        Object[] targetArray;
        if (target instanceof MultiTargetList) {
            targetArray = ((MultiTargetList) target).toArray();
        } else {
            targetArray = new Object[] { target };
        }

        return hasPermission(action, targetArray);
    }

    private boolean hasPermission(String action, Object... targets) {
        return evaluator.checkPermission(action, targets);
    }

}

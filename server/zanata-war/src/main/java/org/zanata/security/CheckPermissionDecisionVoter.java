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
package org.zanata.security;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.PermissionTarget;

import com.google.common.collect.Lists;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequestScoped
public class CheckPermissionDecisionVoter extends
        AbstractAccessDecisionVoter {

    @Inject
    private ZanataIdentity identity;

    @Override
    protected void checkPermission(
            AccessDecisionVoterContext accessDecisionVoterContext,
            Set<SecurityViolation> violations) {
        CheckPermission checkPermission =
                accessDecisionVoterContext.getMetaDataFor(
                        CheckPermission.class.getName(), CheckPermission.class);
        if (checkPermission != null) {
            String permissionName = checkPermission.value();
            InvocationContext invocationCtx =
                    accessDecisionVoterContext.<InvocationContext> getSource();
            List permissionTargets = getPermissionTargets(invocationCtx);
            if (!identity.hasPermission(permissionTargets.toArray(),
                    permissionName)) {
                violations.add(newSecurityViolation("You don't have permission to do this"));
            }

        }
    }

    private List getPermissionTargets(InvocationContext ctx) {
        List targets = Lists.newArrayList();
        Annotation[][] paramAnnotations =
                ctx.getMethod().getParameterAnnotations();
        int pos = 0;
        for (Annotation[] annotsPerParam : paramAnnotations) {
            for (Annotation paramAnnot : annotsPerParam) {
                if (paramAnnot instanceof PermissionTarget) {
                    targets.add(ctx.getParameters()[pos]);
                    break;
                }
            }
            pos++;
        }
        return targets;
    }

}

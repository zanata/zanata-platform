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

import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.zanata.security.annotations.CheckRole;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RequestScoped
public class CheckRoleDecisionVoter extends AbstractAccessDecisionVoter {
    private static final long serialVersionUID = -2225527674677560626L;

    @Inject
    private ZanataIdentity identity;

    @Override
    protected void checkPermission(
            AccessDecisionVoterContext accessDecisionVoterContext,
            Set<SecurityViolation> violations) {

        CheckRole hasRole =
                accessDecisionVoterContext
                        .getMetaDataFor(CheckRole.class.getName(),
                                CheckRole.class);
        if (hasRole != null) {
            boolean result = identity.hasRole(hasRole.value());

            if (!result) {
                boolean loggedIn = identity.isLoggedIn();
                if (!loggedIn) {
                    violations.add(newSecurityViolation("Not logged in"));
                } else {
                    violations.add(CheckRoleSecurityViolation
                            .instance(hasRole.value()));
                }
            }
        }
    }

    public static class CheckRoleSecurityViolation
            implements SecurityViolation {
        private static final String REASON =
                "You don't have the necessary access";
        private String requiredRole;

        public CheckRoleSecurityViolation(String requiredRole) {
            this.requiredRole = requiredRole;
        }

        @Override
        public String getReason() {
            return REASON;
        }

        public String getRequiredRole() {
            return requiredRole;
        }

        static CheckRoleSecurityViolation instance(String requiredRole) {
            return new CheckRoleSecurityViolation(requiredRole);
        }
    }
}

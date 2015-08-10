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
package org.zanata.seam.security;

import java.security.Principal;
import java.security.acl.Group;
import javax.security.auth.Subject;

import org.zanata.security.Identity;
import org.zanata.security.SimpleGroup;
import org.zanata.security.SimplePrincipal;
import org.zanata.security.ZanataIdentity;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class AbstractRunAsOperation implements
        Identity.RunAsOperation {
    private Principal principal;
    private Subject subject;

    private boolean systemOp = false;

    public AbstractRunAsOperation() {
        principal = new SimplePrincipal(null);
        subject = new Subject();
    }

    /**
     * A system operation allows any security checks to pass
     *
     * @param systemOp
     */
    public AbstractRunAsOperation(boolean systemOp) {
        this();
        this.systemOp = systemOp;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    public AbstractRunAsOperation addRole(String role) {
        for (Group sg : getSubject().getPrincipals(Group.class)) {
            if (ZanataIdentity.ROLES_GROUP.equals(sg.getName())) {
                sg.addMember(new SimplePrincipal(role));
                break;
            }
        }

        Group roleGroup = new SimpleGroup(ZanataIdentity.ROLES_GROUP);
        roleGroup.addMember(new SimplePrincipal(role));
        getSubject().getPrincipals().add(roleGroup);

        return this;
    }

    @Override
    public boolean isSystemOperation() {
        return systemOp;
    }

    @Override
    public void run() {
        ZanataIdentity.instance().runAs(this);
    }
}

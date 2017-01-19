/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Represents a dynamic assignment of a role for HAccounts.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Cacheable
public class HRoleAssignmentRule extends ModelEntityBase {

    private String policyName;
    private String identityRegExp;
    private HAccountRole roleToAssign;

    @Column(length = 100)
    public String getPolicyName() {
        return policyName;
    }

    public String getIdentityRegExp() {
        return identityRegExp;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_to_assign_id", nullable = false)
    public HAccountRole getRoleToAssign() {
        return roleToAssign;
    }

    public void setPolicyName(final String policyName) {
        this.policyName = policyName;
    }

    public void setIdentityRegExp(final String identityRegExp) {
        this.identityRegExp = identityRegExp;
    }

    public void setRoleToAssign(final HAccountRole roleToAssign) {
        this.roleToAssign = roleToAssign;
    }

    @Override
    public String toString() {
        return "HRoleAssignmentRule(super=" + super.toString() + ", policyName="
                + this.getPolicyName() + ", identityRegExp="
                + this.getIdentityRegExp() + ", roleToAssign="
                + this.getRoleToAssign() + ")";
    }
}

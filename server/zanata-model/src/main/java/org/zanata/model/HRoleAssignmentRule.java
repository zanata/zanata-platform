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

import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * Represents a dynamic assignment of a role for HAccounts.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Cacheable
@GraphQLType(name = "RoleAssignmentRule")
public class HRoleAssignmentRule extends ModelEntityBase {

    private static final long serialVersionUID = -3893092614851019323L;
    private String policyName;
    private String identityRegExp;
    private HAccountRole roleToAssign;

    @Column(length = 100)
    public String getPolicyName() {
        return policyName;
    }

    @Column(columnDefinition = "longtext")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HRoleAssignmentRule that = (HRoleAssignmentRule) o;

        if (policyName != null ? !policyName.equals(that.policyName) :
                that.policyName != null) return false;
        if (identityRegExp != null ?
                !identityRegExp.equals(that.identityRegExp) :
                that.identityRegExp != null) return false;
        return roleToAssign != null ? roleToAssign.equals(that.roleToAssign) :
                that.roleToAssign == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (policyName != null ? policyName.hashCode() : 0);
        result =
                31 * result +
                        (identityRegExp != null ? identityRegExp.hashCode() :
                                0);
        result = 31 * result +
                (roleToAssign != null ? roleToAssign.hashCode() : 0);
        return result;
    }
}

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
package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Model;
import javax.validation.constraints.NotNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.ConversationGroup;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.annotations.CheckRole;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.model.HRoleAssignmentRule;
import org.zanata.seam.framework.EntityHome;
import org.zanata.security.AuthenticationType;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("roleAssignmentRuleAction")
@RequestScoped
@Model
@Transactional
@CheckRole("admin")
public class RoleAssignmentRuleAction extends EntityHome<HRoleAssignmentRule>
        implements Serializable {
    private static final Logger log =
            LoggerFactory.getLogger(RoleAssignmentRuleAction.class);
    private static final long serialVersionUID = 1L;

    @Inject
    @Any
    private RoleAssignmentRuleId roleAssignmentRuleId;

    @Inject
    private RoleAssignmentRuleDAO roleAssignmentRuleDAO;

    @Inject
    private AccountRoleDAO accountRoleDAO;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    public RoleAssignmentRuleAction() {
        setEntityClass(HRoleAssignmentRule.class);
    }

    @Override
    public void setId(Object id) {
        if (id != null) {
            roleAssignmentRuleId.setId(Long.parseLong(id.toString()));
        } else {
            roleAssignmentRuleId.setId(null);
        }
    }

    @Override
    public Object getId() {
        return roleAssignmentRuleId.getId();
    }

    public List<HRoleAssignmentRule> getAllRules() {
        return roleAssignmentRuleDAO.findAll();
    }

    public void remove(String id) {
        HRoleAssignmentRule rule = roleAssignmentRuleDAO.findById(new Long(id));
        if (rule != null) {
            roleAssignmentRuleDAO.makeTransient(rule);
            roleAssignmentRuleDAO.flush();
        }
    }

    public void setRoleToAssign(String roleName) {
        this.getInstance().setRoleToAssign(accountRoleDAO.findByName(roleName));
    }

    @NotNull
    public String getRoleToAssign() {
        if (this.getInstance() == null
                || this.getInstance().getRoleToAssign() == null) {
            return null;
        } else {
            return this.getInstance().getRoleToAssign().getName();
        }
    }

    public List<String> getAvailablePolicyNames() {
        List<String> policyNames = new ArrayList<String>();

        if (applicationConfiguration.isInternalAuth()) {
            policyNames.add(AuthenticationType.INTERNAL.name());
        }
        if (applicationConfiguration.isOpenIdAuth()) {
            policyNames.add(AuthenticationType.OPENID.name());
        }
        if (applicationConfiguration.isJaasAuth()) {
            policyNames.add(AuthenticationType.JAAS.name());
        }
        if (applicationConfiguration.isKerberosAuth()) {
            policyNames.add(AuthenticationType.KERBEROS.name());
        }

        return policyNames;
    }
}

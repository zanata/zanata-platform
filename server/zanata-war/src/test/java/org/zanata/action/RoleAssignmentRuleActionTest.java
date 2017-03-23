/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.RoleAssignmentRuleDAO;
import org.zanata.model.HRoleAssignmentRule;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@InSessionScope
@RunWith(CdiUnitRunner.class)
public class RoleAssignmentRuleActionTest {

    @Mock
    @Produces
    @Any
    private RoleAssignmentRuleId roleAssignmentRuleId;

    @Produces
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RoleAssignmentRuleDAO roleAssignmentRuleDAO;

    @Mock
    @Produces
    private AccountRoleDAO accountRoleDAO;

    @Mock
    @Produces
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    @Produces
    private UserTransaction transaction;

    @Mock
    @Produces
    private EntityManager entityManager;

    @Mock
    @Produces
    private FacesMessages facesMessages;

    @Inject
    RoleAssignmentRuleAction action;

    @Test
    public void testRemove() {
        HRoleAssignmentRule rule = new HRoleAssignmentRule();
        rule.setId(0L);
        rule.setPolicyName("INTERNAL");
        when(roleAssignmentRuleDAO.findById(any())).thenReturn(rule);
        action.remove("0");
        verify(roleAssignmentRuleDAO, times(1)).makeTransient(rule);
        verify(roleAssignmentRuleDAO, times(1)).flush();
    }
}

/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.util;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EssentialDataCreatorTest {
    private EssentialDataCreator creator;
    @Mock
    private AccountRoleDAO accountRoleDAO;
    @Mock
    private HAccountRole role;
    @Mock
    private LocaleDAO localeDAO;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private AccountDAO accountDAO;
    @Mock
    private HLocale enUS;
    @Mock
    private HAccount adminAccount;
    @Mock
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        creator = new EssentialDataCreator(applicationConfiguration, accountDAO, accountRoleDAO, localeDAO, applicationConfigurationDAO);
        when(localeDAO.makePersistent(any(HLocale.class))).thenReturn(enUS);
    }

    @Test
    public void canCreateAllRoles() {
        givenRolesDoNotExists("admin", "user", "glossarist", "glossary-admin", "project-creator");

        creator.prepare();

        verify(accountRoleDAO).create("user", HAccountRole.RoleType.MANUAL, "project-creator");
        verify(accountRoleDAO).create("glossarist", HAccountRole.RoleType.MANUAL);
        verify(accountRoleDAO).create("glossary-admin", HAccountRole.RoleType.MANUAL, "glossarist");
        verify(accountRoleDAO).create("admin", HAccountRole.RoleType.MANUAL, "user", "glossary-admin");
        verify(accountRoleDAO).create("project-creator", HAccountRole.RoleType.MANUAL);
    }

    @Test
    public void willNotCreateRolesIfTheyExists() {
        givenRolesDoExists("admin", "user", "glossarist", "glossary-admin", "project-creator");

        creator.prepare();

        verify(accountRoleDAO, never()).create(anyString(), eq(HAccountRole.RoleType.MANUAL), Matchers
                .<String> anyVararg());
    }

    @Test
    public void willGiveAdminRolesToConfiguredAdminUsers() {
        when(accountRoleDAO.roleExists(anyString())).thenReturn(true);

        when(applicationConfiguration.getAdminUsers()).thenReturn(
                Sets.newHashSet("redhat"));
        when(accountDAO.getByUsername("redhat")).thenReturn(adminAccount);
        Set<HAccountRole> rolesForAccount = Sets.newHashSet();
        when(adminAccount.getRoles()).thenReturn(rolesForAccount);
        when(accountRoleDAO.findByName("admin")).thenReturn(role);

        creator.prepare();

        verify(accountDAO).makePersistent(adminAccount);
        verify(accountDAO).flush();
        assertThat(rolesForAccount).contains(role);
    }

    private void givenRolesDoExists(String... roleNames) {
        for (String roleName : roleNames) {
            when(accountRoleDAO.roleExists(roleName)).thenReturn(true);
        }
    }

    private void givenRolesDoNotExists(String... roleNames) {
        for (String roleName : roleNames) {
            when(accountRoleDAO.roleExists(roleName)).thenReturn(false);
            when(accountRoleDAO.create(anyString(), eq(HAccountRole.RoleType.MANUAL), Matchers
                    .<String> anyVararg())).thenReturn(role);
        }
    }

}

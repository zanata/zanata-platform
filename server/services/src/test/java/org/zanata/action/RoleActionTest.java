/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.commons.lang.RandomStringUtils;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@InSessionScope
@RunWith(CdiUnitRunner.class)
public class RoleActionTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Mock
    @Produces
    private IdentityManager identityManager;

    @Mock
    @Produces
    private ZanataIdentity identity;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Produces
    FacesMessages facesMessages;

    @Mock
    UIComponent uiComponent;

    @Inject
    private RoleAction roleAction;

    @Test
    public void saveFailWhenBlankID() {
        roleAction.setRole(null);
        assertThat(roleAction.save()).isEqualTo("failure");

        when(identityManager.roleExists("  ")).thenReturn(false);
        roleAction.setRole("  ");
        assertThat(roleAction.save()).isEqualTo("failure");
    }

    @Test
    public void cantCreateRoleWithDuplicateRoleName() {
        roleAction.setRole(null);
        roleAction.loadRole();
        roleAction.setRole("admin");
        when(identityManager.roleExists("admin")).thenReturn(true);
        assertThat(roleAction.save()).isEqualTo("failure");
    }

    @Test
    public void cantModifyRoleName() {
        roleAction.setRole("admin");
        roleAction.loadRole();
        roleAction.setRole("admin1");
        when(identityManager.roleExists("admin1")).thenReturn(true);
        assertThat(roleAction.save()).isEqualTo("failure");
    }

    @Test
    public void duplicateRoleNameFailsValidation() {
        when(identityManager.roleExists("admin")).thenReturn(true);
        roleAction.setRole(null);
        roleAction.loadRole();
        String newValue = "admin";
        assertThat(roleAction.validateRoleName(
                new ValueChangeEvent(uiComponent, "", newValue))).isFalse();
    }

    @Test
    public void longRoleNameFailsValidation() {
        String newValue = RandomStringUtils.random(256, true, true);
        assertThat(newValue.length()).isEqualTo(256);
        assertThat(roleAction.validateRoleName(
                new ValueChangeEvent(uiComponent, "", newValue))).isFalse();
    }

    @Test
    public void modifiedRoleNameFailsValidation() {
        // Because we disallow role renaming
        roleAction.setRole("admin");
        roleAction.loadRole();
        assertThat(roleAction.validateRoleName(
                new ValueChangeEvent(uiComponent, "admin", "notAdmin")))
                .isFalse();
    }

    @Test
    public void validateValidRole() {
        roleAction.setRole(null);
        roleAction.loadRole();
        when(identityManager.roleExists("test")).thenReturn(false);
        assertThat(roleAction.validateRoleName(
                new ValueChangeEvent(uiComponent, "", "test"))).isTrue();
    }
}

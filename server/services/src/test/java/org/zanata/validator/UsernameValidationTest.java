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
package org.zanata.validator;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.action.RegisterAction;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.anyString;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(DataProviderRunner.class)
public class UsernameValidationTest {

    private static Validator validator;

    private RegisterAction registerAction = mock(RegisterAction.class);

    @DataProvider
    public static Object[][] badUserNames() {
        Object[][] names = {
                { "user|name" },
                { "user/name" },
                { "user\\name" },
                { "user+name" },
                { "user*name" },
                { "user(name" },
                { "user)name" },
                { "user$name" },
                { "user[name" },
                { "user]name" },
                { "user:name" },
                { "user;name" },
                { "user'name" },
                { "user,name" },
                { "user?name" },
                { "user!name" },
                { "user@name" },
                { "user#name" },
                { "user%name" },
                { "user^name" },
                { "user=name" },
                { "user.name" },
                { "user{name" },
                { "user}name" },
                { "userAname" },
                { "userZname" },
                { "abcdefghijklmnopqrstuvwxyz"},
                { "bo" },
                { "" }
        };
        return names;
    }

    @DataProvider
    public static Object[][] goodUserNames() {
        Object[][] names = {
                { "username" },
                { "user0name" },
                { "user_name" },
                { "u12345e" },
                { "0abcde1" },
                { "123" },
                { "12345678901234567890" }
        };
        return names;
    }

    @BeforeClass
    public static void initValidator() {
        try {
            validator = buildNonCDIValidatorFactory().getValidator();
        } catch (NullPointerException npe) {
            throw new RuntimeException("Failed to build validator", npe);
        }
    }

    /**
     * The CDIAwareConstraintValidatorFactory configured in validation.xml
     * requires DeltaSpike and CDI (and thus CdiUnitRunner).
     * @return
     */
    private static ValidatorFactory buildNonCDIValidatorFactory() {
        return Validation.byDefaultProvider().configure().ignoreXmlConfiguration().buildValidatorFactory();
    }

    @Test
    @UseDataProvider("badUserNames")
    public void failInvalidUsername(String username) {
        doNothing().when(registerAction).validateUsername(anyString());
        doCallRealMethod().when(registerAction).setUsername(anyString());
        doCallRealMethod().when(registerAction).getUsername();
        registerAction.setUsername(username);

        assertThat(registerAction.getUsername()).isEqualTo(username);

        Set<ConstraintViolation<RegisterAction>> constraintViolations =
                validator.validateProperty(registerAction, "username");

        assertThat(constraintViolations.size())
                .isGreaterThanOrEqualTo(1) // May cause multiple violations
                .as("The username failed validation");
    }

    @Test
    @UseDataProvider("goodUserNames")
    public void acceptValidUsername(String username) {
        doNothing().when(registerAction).validateUsername(anyString());
        doCallRealMethod().when(registerAction).setUsername(anyString());
        doCallRealMethod().when(registerAction).getUsername();
        registerAction.setUsername(username);

        assertThat(registerAction.getUsername()).isEqualTo(username);

        Set<ConstraintViolation<RegisterAction>> constraintViolations =
                validator.validateProperty(registerAction, "username");

        assertThat(constraintViolations.size())
                .isEqualTo(0)
                .as("The username passed validation");
    }

}

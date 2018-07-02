/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.account

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.zanata.feature.account.comp.InactiveUserLoginCTest
import org.zanata.feature.account.comp.RegisterCTest
import org.zanata.feature.account.comp.RegisterUsernameCharactersCTest

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(ChangePasswordTest::class,
        EmailValidationTest::class,
        InactiveUserLoginTest::class,
        ProfileTest::class, RegisterTest::class,
        UsernameValidationTest::class,
        RegisterTest::class,
        //Comprehensive tests
        InactiveUserLoginCTest::class,
        RegisterCTest::class,
        RegisterUsernameCharactersCTest::class)
class AccountTestSuite

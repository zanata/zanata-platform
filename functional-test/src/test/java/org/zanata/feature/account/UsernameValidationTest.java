/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.account;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.zanata.page.account.RegisterPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Theories.class)
public class UsernameValidationTest
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @DataPoint public static String INVALID_PIPE = "user|name";
   @DataPoint public static String INVALID_SLASH = "user/name";
   @DataPoint public static String INVALID_BACKSLASH = "user\\name";
   @DataPoint public static String INVALID_PLUS = "user+name";
   @DataPoint public static String INVALID_ASTERISK = "user*name";
   @DataPoint public static String INVALID_LEFT_PARENTHESES = "user(name";
   @DataPoint public static String INVALID_RIGHT_PARENTHESES = "user)name";
   @DataPoint public static String INVALID_DOLLAR = "user$name";
   @DataPoint public static String INVALID_LEFT_BRACKET = "user[name";
   @DataPoint public static String INVALID_RIGHT_BRACKET = "user]name";
   @DataPoint public static String INVALID_COLON = "user:name";
   @DataPoint public static String INVALID_SEMICOLON = "user;name";
   @DataPoint public static String INVALID_APOSTROPHE = "user'name";
   @DataPoint public static String INVALID_COMMA = "user,name";
   @DataPoint public static String INVALID_QUESTION_MARK = "user?name";
   @DataPoint public static String INVALID_EXCLAMATION_MARK = "user!name";
   @DataPoint public static String INVALID_AMPERSAT = "user@name";
   @DataPoint public static String INVALID_HASH = "user#name";
   @DataPoint public static String INVALID_PERCENT = "user%name";
   @DataPoint public static String INVALID_CARAT = "user^name";
   @DataPoint public static String INVALID_EQUALS = "user=name";
   @DataPoint public static String INVALID_PERIOD = "user.name";
   @DataPoint public static String INVALID_LEFT_BRACE = "user{name";
   @DataPoint public static String INVALID_RIGHT_BRACE = "user}name";
   @DataPoint public static String INVALID_CAPITAL_A = "userAname";
   @DataPoint public static String INVALID_CAPITAL_Z = "userZname";

   @Theory
   public void usernameCharacterValidation(String username)
   {
      String errorMsg = "lowercase letters and digits (regex \"^[a-z\\d_]{3,20}$\")";
      RegisterPage registerPage = new BasicWorkFlow().goToHome().goToRegistration();
      registerPage = registerPage.enterUserName(username).clickTerms();
      assertThat("Validation errors are shown", registerPage.getErrors(), Matchers.hasItem(errorMsg));
   }
}

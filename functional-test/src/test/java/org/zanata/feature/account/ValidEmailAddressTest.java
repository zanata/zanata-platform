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
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.RegisterPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.util.rfc2822.ValidEmailAddressRFC2822;
import org.zanata.workflow.BasicWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.rfc2822.ValidEmailAddressRFC2822.*;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Theories.class)
@Category(DetailedTest.class)
public class ValidEmailAddressTest {

   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @DataPoint public static ValidEmailAddressRFC2822 TEST_BASIC_EMAIL = BASIC_EMAIL;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_SPECIAL_LOCALPART_CHARACTERS = SPECIAL_CHARACTERS_LOCALPART;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_LOCALPART_MULTIPLE_LABELS = LOCALPART_MULTIPLE_LABELS;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_DOMAIN_MULTIPLE_LABELS = DOMAIN_MULTIPLE_LABELS;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_DOMAIN_LABEL_MAX_CHARACTERS = DOMAIN_LABEL_MAX_CHARACTERS;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_LOCALPART_LABEL_MAX_CHARACTERS = LOCALPART_LABEL_MAX_CHARACTERS;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_HYPHENATED_DOMAIN_LABEL = HYPHENATED_DOMAIN_LABEL;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_HYPHENATED_LOCALPART_LABEL = HYPHENATED_LOCALPART_LABEL;
   @DataPoint public static ValidEmailAddressRFC2822 TEST_LOCALPART_MAX_LENGTH = LOCALPART_MAX_LENGTH;
   
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_BASIC_QUOTED_EMAIL = BASIC_QUOTED_EMAIL;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_ENCLOSED_QUOTED_LABEL = ENCLOSED_QUOTED_LABEL;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_LOCALPART_EMPTY_QUOTE = LOCALPART_WITH_EMPTY_QUOTE;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_QUOTED_ESCAPED_SPECIAL_CHARACTERS = QUOTED_ESCAPED_SPECIAL_CHARACTERS;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_QUOTED_ESCAPED_QUOTES = QUOTED_ESCAPED_QUOTES;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_QUOTED_WITH_SPACE = QUOTED_WITH_SPACE;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_BRACKETED_DOMAIN = BRACKETED_DOMAIN;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_BRACKETED_IPV4_DOMAIN = BRACKETED_IPV4_DOMAIN;
   // BUG982048 @DataPoint public static ValidEmailAddressRFC2822 TEST_BRACKETED_IPV6_DOMAIN = BRACKETED_IPV6_DOMAIN;

   @Theory
   public void validEmailAcceptance(ValidEmailAddressRFC2822 emailAddress)
   {
      String errorMsg = "not a well-formed email address";
      RegisterPage registerPage = new BasicWorkFlow().goToHome().goToRegistration();
      registerPage = registerPage.enterEmail(emailAddress.toString()).clickTerms();
      assertThat("Email validation errors are not shown", registerPage.getErrors(),
            Matchers.not(Matchers.hasItem(errorMsg)));

   }

}

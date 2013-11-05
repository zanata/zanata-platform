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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.zanata.feature.DetailedTest;
import org.zanata.page.account.RegisterPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.util.rfc2822.InvalidEmailAddressRFC2822;
import org.zanata.workflow.BasicWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.rfc2822.InvalidEmailAddressRFC2822.*;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Theories.class)
@Category(DetailedTest.class)
public class InvalidEmailAddressTest {
    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_PLAIN_ADDRESS = PLAIN_ADDRESS;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_MISSING_AMPERSAT =
            MISSING_AMPERSAT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_MISSING_LOCALPART =
            MISSING_LOCALPART;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_MISSING_DOMAIN =
            MISSING_DOMAIN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_MULTIPLE_APERSAT =
            MULTIPLE_APERSAT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_LEADING_DOT = LEADING_DOT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_TRAILING_DOT = TRAILING_DOT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_MULTIPLE_DOTS = MULTIPLE_DOTS;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_UNQUOTED_COMMA =
            INVALID_UNQUOTED_COMMA;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_UNQUOTED_LEFT_PARENTHESES =
            INVALID_UNQUOTED_LEFT_PARENTHESES;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_UNQUOTED_RIGHT_PARENTHESES =
            INVALID_UNQUOTED_RIGHT_PARENTHESES;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_SINGLE_QUOTING =
            INVALID_SINGLE_QUOTING;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTING_SEPARATION =
            INVALID_QUOTING_SEPARATION;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_COMMA =
            INVALID_QUOTED_COMMA;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_BACKSLASH =
            INVALID_QUOTED_BACKSLASH;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_LEFT_BRACKET =
            INVALID_QUOTED_LEFT_BRACKET;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_RIGHT_BRACKET =
            INVALID_QUOTED_RIGHT_BRACKET;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_CARAT =
            INVALID_QUOTED_CARAT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_SPACE =
            INVALID_QUOTED_SPACE;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_QUOTE =
            INVALID_QUOTED_QUOTE;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_RETURN =
            INVALID_QUOTED_RETURN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_QUOTED_LINEFEED =
            INVALID_QUOTED_LINEFEED;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_TRAILING_DOMAIN_DOT =
            TRAILING_DOMAIN_DOT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_LEADING_DOMAIN_DOT =
            LEADING_DOMAIN_DOT;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_SUCCESSIVE_DOMAIN_DOTS =
            SUCCESSIVE_DOMAIN_DOTS;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INCORRECTLY_BRACKETED_DOMAIN =
            INCORRECTLY_BRACKETED_DOMAIN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_DOMAIN_CHARACTER =
            INVALID_DOMAIN_CHARACTER;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INCORRECTLY_ESCAPED_DOMAIN =
            INCORRECTLY_ESCAPED_DOMAIN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_DOMAIN_LABEL_LENGTH_EXCEEDED =
            DOMAIN_LABEL_LENGTH_EXCEEDED;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_LEADING_DASH_BRACKETED_DOMAIN =
            LEADING_DASH_BRACKETED_DOMAIN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_TRAILING_DASH_BRACKETED_DOMAIN =
            TRAILING_DASH_BRACKETED_DOMAIN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_MULTIPLE_DASHES_BRACKETED_DOMAIN =
            MULTIPLE_DASHES_BRACKETED_DOMAIN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_BRACKETED_DOMAIN_RETURN =
            INVALID_BRACKETED_DOMAIN_RETURN;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_BRACKETED_DOMAIN_LINEFEED =
            INVALID_BRACKETED_DOMAIN_LINEFEED;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_LOCALPART_LENGTH_EXCEEDED =
            LOCALPART_LENGTH_EXCEEDED;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_ENCODED_HTML =
            INVALID_ENCODED_HTML;
    @DataPoint
    public static InvalidEmailAddressRFC2822 TEST_INVALID_FOLLOWING_TEXT =
            INVALID_FOLLOWING_TEXT;

    // BUG982048 @DataPoint public static InvalidEmailAddressRFC2822
    // TEST_INVALID_IP_FORMAT = INVALID_IP_FORMAT;
    // BUG982048 @DataPoint public static InvalidEmailAddressRFC2822
    // TEST_MAX_EMAIL_LENGTH_EXCEEDED = MAX_EMAIL_LENGTH_EXCEEDED;
    // BUG982048 @DataPoint public static InvalidEmailAddressRFC2822
    // TEST_NON_UNICODE_CHARACTERS = NON_UNICODE_CHARACTERS;
    // BUG982048 @DataPoint public static InvalidEmailAddressRFC2822
    // TEST_LEADING_DASH_DOMAIN = LEADING_DASH_DOMAIN;
    // BUG982048 @DataPoint public static InvalidEmailAddressRFC2822
    // TEST_TRAILING_DASH_DOMAIN = TRAILING_DASH_DOMAIN;
    // BUG982048 @DataPoint public static InvalidEmailAddressRFC2822
    // TEST_MULTIPLE_DASHES_DOMAIN = MULTIPLE_DASHES_DOMAIN;

    @Before
    public void setUp() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
    }

    @Theory
    public void invalidEmailRejection(InvalidEmailAddressRFC2822 emailAddress) {
        String errorMsg = "not a well-formed email address";
        RegisterPage registerPage =
                new BasicWorkFlow().goToHome().goToRegistration();
        registerPage = registerPage.enterEmail(emailAddress.toString());
        registerPage.defocus();

        assertThat("Email validation errors are not shown",
                registerPage.waitForFieldErrors(), Matchers.hasItem(errorMsg));
    }

}

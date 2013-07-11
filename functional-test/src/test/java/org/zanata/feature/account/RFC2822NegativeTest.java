package org.zanata.feature.account;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.zanata.page.account.RegisterPage;
import org.zanata.util.RFC2822;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.BasicWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Theories.class)
public class RFC2822NegativeTest {
   @DataPoint public static String PLAIN_ADDRESS = RFC2822.PLAIN_ADDRESS;
   @DataPoint public static String MISSING_AMPERSAT = RFC2822.MISSING_AMPERSAT;
   @DataPoint public static String MISSING_LOCALPART = RFC2822.MISSING_LOCALPART;
   @DataPoint public static String MISSING_DOMAIN = RFC2822.MISSING_DOMAIN;
   @DataPoint public static String MULTIPLE_APERSAT = RFC2822.MULTIPLE_APERSAT;
   @DataPoint public static String LEADING_DOT = RFC2822.LEADING_DOT;
   @DataPoint public static String TRAILING_DOT = RFC2822.TRAILING_DOT;
   @DataPoint public static String MULTIPLE_DOTS = RFC2822.MULTIPLE_DOTS;
   @DataPoint public static String INVALID_UNQUOTED_COMMA = RFC2822.INVALID_UNQUOTED_COMMA;
   @DataPoint public static String INVALID_UNQUOTED_LEFT_PARENTHESES = RFC2822.INVALID_UNQUOTED_LEFT_PARENTHESES;
   @DataPoint public static String INVALID_UNQUOTED_RIGHT_PARENTHESES = RFC2822.INVALID_UNQUOTED_RIGHT_PARENTHESES;
   @DataPoint public static String INVALID_SINGLE_QUOTING = RFC2822.INVALID_SINGLE_QUOTING;
   @DataPoint public static String INVALID_QUOTING_SEPARATION = RFC2822.INVALID_QUOTING_SEPARATION;
   @DataPoint public static String INVALID_QUOTED_COMMA = RFC2822.INVALID_QUOTED_COMMA;
   @DataPoint public static String INVALID_QUOTED_BACKSLASH = RFC2822.INVALID_QUOTED_BACKSLASH;
   @DataPoint public static String INVALID_QUOTED_LEFT_BRACKET = RFC2822.INVALID_QUOTED_LEFT_BRACKET;
   @DataPoint public static String INVALID_QUOTED_RIGHT_BRACKET = RFC2822.INVALID_QUOTED_RIGHT_BRACKET;
   @DataPoint public static String INVALID_QUOTED_CARAT = RFC2822.INVALID_QUOTED_CARAT;
   @DataPoint public static String INVALID_QUOTED_SPACE = RFC2822.INVALID_QUOTED_SPACE;
   @DataPoint public static String INVALID_QUOTED_QUOTE = RFC2822.INVALID_QUOTED_QUOTE;
   @DataPoint public static String INVALID_QUOTED_RETURN = RFC2822.INVALID_QUOTED_RETURN;
   @DataPoint public static String INVALID_QUOTED_LINEFEED = RFC2822.INVALID_QUOTED_LINEFEED;
   @DataPoint public static String TRAILING_DOMAIN_DOT = RFC2822.TRAILING_DOMAIN_DOT;
   @DataPoint public static String LEADING_DOMAIN_DOT = RFC2822.LEADING_DOMAIN_DOT;
   @DataPoint public static String SUCCESSIVE_DOMAIN_DOTS = RFC2822.SUCCESSIVE_DOMAIN_DOTS;
   @DataPoint public static String INCORRECTLY_BRACKETED_DOMAIN = RFC2822.INCORRECTLY_BRACKETED_DOMAIN;
   @DataPoint public static String INVALID_DOMAIN_CHARACTER = RFC2822.INVALID_DOMAIN_CHARACTER;
   @DataPoint public static String INCORRECTLY_ESCAPED_DOMAIN = RFC2822.INCORRECTLY_ESCAPED_DOMAIN;
   @DataPoint public static String DOMAIN_LABEL_LENGTH_EXCEEDED = RFC2822.DOMAIN_LABEL_LENGTH_EXCEEDED;
   @DataPoint public static String LEADING_DASH_BRACKETED_DOMAIN = RFC2822.LEADING_DASH_BRACKETED_DOMAIN;
   @DataPoint public static String TRAILING_DASH_BRACKETED_DOMAIN = RFC2822.TRAILING_DASH_BRACKETED_DOMAIN;
   @DataPoint public static String MULTIPLE_DASHES_BRACKETED_DOMAIN = RFC2822.MULTIPLE_DASHES_BRACKETED_DOMAIN;
   @DataPoint public static String INVALID_BRACKETED_DOMAIN_RETURN = RFC2822.INVALID_BRACKETED_DOMAIN_RETURN;
   @DataPoint public static String INVALID_BRACKETED_DOMAIN_LINEFEED = RFC2822.INVALID_BRACKETED_DOMAIN_LINEFEED;
   @DataPoint public static String LOCALPART_LENGTH_EXCEEDED = RFC2822.LOCALPART_LENGTH_EXCEEDED;
   @DataPoint public static String INVALID_ENCODED_HTML = RFC2822.INVALID_ENCODED_HTML;
   @DataPoint public static String INVALID_FOLLOWING_TEXT = RFC2822.INVALID_FOLLOWING_TEXT;

   // BUG982048 @DataPoint public static String INVALID_IP_FORMAT = RFC2822.INVALID_IP_FORMAT;
   // BUG982048 @DataPoint public static String MAX_EMAIL_LENGTH_EXCEEDED = RFC2822.MAX_EMAIL_LENGTH_EXCEEDED;
   // BUG982048 @DataPoint public static String NON_UNICODE_CHARACTERS = RFC2822.NON_UNICODE_CHARACTERS;
   // BUG982048 @DataPoint public static String LEADING_DASH_DOMAIN = RFC2822.LEADING_DASH_DOMAIN;
   // BUG982048 @DataPoint public static String TRAILING_DASH_DOMAIN = RFC2822.TRAILING_DASH_DOMAIN;
   // BUG982048 @DataPoint public static String MULTIPLE_DASHES_DOMAIN = RFC2822.MULTIPLE_DASHES_DOMAIN;

   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @Theory
   public void invalidEmailRejection(String emailAddress)
   {
      String errorMsg = "not a well-formed email address";
      RegisterPage registerPage = new BasicWorkFlow().goToHome().goToRegistration();
      registerPage = registerPage.enterEmail(emailAddress).clickTerms();
      assertThat("Email validation errors are not shown", registerPage.waitForErrors(), Matchers.hasItem(errorMsg));
   }

}

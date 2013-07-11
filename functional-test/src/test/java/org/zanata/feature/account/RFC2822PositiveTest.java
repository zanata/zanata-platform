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
public class RFC2822PositiveTest {

   @DataPoint public static String BASIC_EMAIL = RFC2822.BASIC_EMAIL;
   @DataPoint public static String SPECIAL_LOCALPART_CHARACTERS = RFC2822.SPECIAL_CHARACTERS_LOCALPART;
   @DataPoint public static String LOCALPART_MULTIPLE_LABELS = RFC2822.LOCALPART_MULTIPLE_LABELS;
   @DataPoint public static String DOMAIN_MULTIPLE_LABELS = RFC2822.DOMAIN_MULTIPLE_LABELS;
   @DataPoint public static String DOMAIN_LABEL_MAX_CHARACTERS = RFC2822.DOMAIN_LABEL_MAX_CHARACTERS;
   @DataPoint public static String LOCALPART_LABEL_MAX_CHARACTERS = RFC2822.LOCALPART_LABEL_MAX_CHARACTERS;
   @DataPoint public static String HYPHENATED_DOMAIN_LABEL = RFC2822.HYPHENATED_DOMAIN_LABEL;
   @DataPoint public static String HYPHENATED_LOCALPART_LABEL = RFC2822.HYPHENATED_LOCALPART_LABEL;
   @DataPoint public static String LOCALPART_MAX_LENGTH = RFC2822.LOCALPART_MAX_LENGTH;

   // BUG982048 @DataPoint public static String BASIC_QUOTED_EMAIL = RFC2822.BASIC_QUOTED_EMAIL;
   // BUG982048 @DataPoint public static String ENCLOSED_QUOTED_LABEL = RFC2822.ENCLOSED_QUOTED_LABEL;
   // BUG982048 @DataPoint public static String LOCALPART_EMPTY_QUOTE = RFC2822.LOCALPART_WITH_EMPTY_QUOTE;
   // BUG982048 @DataPoint public static String QUOTED_ESCAPED_SPECIAL_CHARACTERS = RFC2822.QUOTED_ESCAPED_SPECIAL_CHARACTERS;
   // BUG982048 @DataPoint public static String QUOTED_ESCAPED_QUOTES = RFC2822.QUOTED_ESCAPED_QUOTES;
   // BUG982048 @DataPoint public static String QUOTED_WITH_SPACE = RFC2822.QUOTED_WITH_SPACE;
   // BUG982048 @DataPoint public static String BRACKETED_DOMAIN = RFC2822.BRACKETED_DOMAIN;
   // BUG982048 @DataPoint public static String BRACKETED_IPV4_DOMAIN = RFC2822.BRACKETED_IPV4_DOMAIN;
   // BUG982048 @DataPoint public static String BRACKETED_IPV6_DOMAIN = RFC2822.BRACKETED_IPV6_DOMAIN;

   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

   @Theory
   public void validEmailAcceptance(String emailAddress)
   {
      String errorMsg = "not a well-formed email address";
      RegisterPage registerPage = new BasicWorkFlow().goToHome().goToRegistration();
      registerPage = registerPage.enterEmail(emailAddress).clickTerms();
      assertThat("Email validation errors are not shown", registerPage.getErrors(),
            Matchers.not(Matchers.hasItem(errorMsg)));

   }

}

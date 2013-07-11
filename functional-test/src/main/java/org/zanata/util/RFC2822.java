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
package org.zanata.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class RFC2822 {

   /*
    * Synopsis:
    * The functions of this class contain valid and invalid email addresses, as stipulated in the
    * RFC2822 Internet Message Format standard, or referred to standards.
    *
    * Definitions
    * localpart: the section of an address preceding the @ symbol
    * domain: the section of an address following the @ symbol
    * label: section of localpart or domain between the start, @ symbol, period or end (also referred to as "atom")
    *     e.g. me, myself, example, com in me.myself@example.com
    * quote / quoting: a section of the localpart contained within quotation marks
    *
    * Untested:
    * RFC2822, section 3.4.1
    * The contents of a bracketed domain can have a \ precede a character to escape it,
    * and the following character must not be 10 (LF) or 13 (CR).
    * This allows spaces in the domain as long as they are escaped.
    *
    * RFC 2821, section 4.5.3.1
    * The maximum length of a "useful" email address is 255 characters.
    *
    * RFC 3696
    * The maximum allowable length of an email address is 320 characters.
    */

   /*
    * VALID EMAIL ADDRESSES
    */
   /*
    * RFC 2822, section 3.4.1
    * Email addresses consist of a local part, the "@" symbol, and the domain.
    */
   public static String BASIC_EMAIL = "email@example.com";

   /*
    * RFC 2822, sections 3.4.1 and 4.4
    * The local part can be unquoted, quoted in its entirety, or quoted on a per-label basis.
    * The quoted local part starts with a quotation mark, ends with a quotation mark.
    */
   // BUG982048
   public static String BASIC_QUOTED_EMAIL = "\"email\"@example.com";

   /*
    * RFC 2822, section 3.4.1
    * TEXT can contain alphabetic, numeric, and these symbols: !#$%'*+-/=?^_`{|}~
    */
   public static String SPECIAL_CHARACTERS_LOCALPART = "email.!#$%'*+-/=?^_`{|}~.dot@example.com";

   /*
      RFC 2822, section 4.4
      If an email is using the obsolete quoting on a per-label basis, then the email address consists of unquoted
      or quoted chunks separated by periods.
    */
   public static String ENCLOSED_QUOTED_LABEL = "dot.\"email\".dot@example.com";
   public static String LOCALPART_WITH_EMPTY_QUOTE = "dot.\"\".dot@example.com";

   /*
    * RFC 2822, section 3.4.1
    * If the quoted local part has a backslash, the following character is escaped and must not be 10 (LF), 13 (CR).
    * This supersedes the previous rule, allowing spaces and quotation marks in the email address as long as they
    * are escaped.
    */
   public static String QUOTED_ESCAPED_SPECIAL_CHARACTERS = "email.\"(),:;<>\\@\\[\\]\\\\\"@example.com";
   public static String QUOTED_ESCAPED_QUOTES = "email.\"\\\"\"@example.com";
   public static String QUOTED_WITH_SPACE = "\"special\\ email\"@example.com";

   /*
    * RFC 2822, section 3.4.1
    * The domain can be bracketed or plain.
    */
   public static String BRACKETED_DOMAIN = "email@[example.com]";
   public static String BRACKETED_IPV4_DOMAIN = "email@[123.45.67.89]";
   public static String BRACKETED_IPV6_DOMAIN = "email@[IPv6:2001:2d12:c4fe:5afe::1]";

   /*
    * RFC 1035, section 2.3.4
    * A plain domain consists of labels separated with periods. No period can start or end a domain name.
    */
   public static String LOCALPART_MULTIPLE_LABELS = "another.email@example.com";
   public static String DOMAIN_MULTIPLE_LABELS = "email@another.example.com";

   /*
    * RFC 1035, section 2.3.4
    * The maximum length of a label is 63 characters.
    */
   public static String DOMAIN_LABEL_MAX_CHARACTERS =
      "email@B3NQyUsDdzODMoymfDdifn6Wztx2wrivm80LEngHGl182frm6ifCPyv5SntbDg8.com";
   public static String LOCALPART_LABEL_MAX_CHARACTERS =
      "B3NQyUsDdzODMoymfDdifn6Wztx2wrivm80LEngHGl182frm6ifCPyv5SntbDg8@example.com";

      /*
       * RFC 1035, section 2.3.4
       * A label may contain hyphens, but no two hyphens in a row.
       */
   public static String HYPHENATED_DOMAIN_LABEL = "email@another-example.com";
   public static String HYPHENATED_LOCALPART_LABEL = "my-email@example.com";

   /*
    * RFC 2821, section 4.5.3.1
    * The maximum length of the local part is 64 characters.
    */
   public static String LOCALPART_MAX_LENGTH =
      "B3NQyUsDdzODMoymfDdifn6Wztx2wrivm.80LEngHGl182frm6ifCPyv5SntbDg8@example.com";


   /*
    * INVALID EMAIL ADDRESSES
    */

   /*
    * RFC 2822, section 3.4.1
    * Email addresses consist of a local part, the "@" symbol, and the domain.
    */
   public static String PLAIN_ADDRESS = "plainaddress";
   public static String MISSING_AMPERSAT = "email.example.com";
   public static String MISSING_LOCALPART = "@example.com";
   public static String MISSING_DOMAIN = "email@";
   public static String MULTIPLE_APERSAT = "email@example@example.com";

   /*
    * RFC 2822, section 3.4.1
    * No periods can start or end the local part.
    * Two periods together is invalid.
    */
   public static String LEADING_DOT = ".email@example.com";
   public static String TRAILING_DOT = "email.@example.com";
   public static String MULTIPLE_DOTS = "email..email@example.com";

   /*
    * RFC 2822, section 2.2
    * All email addresses are in 7-bit US ASCII.
    */
   public static String NON_UNICODE_CHARACTERS = "あいうえお@example.com";

   /*
    * RFC 2822, section 3.4.1
    * Unquoted local parts can consist of TEXT
    * TEXT can contain:
    *    alphabetic
    *    numeric
    *    and symbols !#$%'*+-/=?^_`{|}~
    */
   public static String INVALID_UNQUOTED_COMMA = "test,user@example.com";
   public static String INVALID_UNQUOTED_LEFT_PARENTHESES = "test(user@example.com";
   public static String INVALID_UNQUOTED_RIGHT_PARENTHESES = "test)user@example.com";

   /*
    * RFC 2822, section 3.4.1
    * The quoted local part starts with a quotation mark, ends with a quotation mark.
    */
   public static String INVALID_SINGLE_QUOTING = "test\"user@example.com";

   /*
    * RFC 2822, section 4.4
    * If an email is using the obsolete quoting on a per-label basis, then the email address consists of unquoted
    * or quoted chunks separated by periods
    */
   public static String INVALID_QUOTING_SEPARATION = "\"test\"user@example.com";

   /*
    * RFC 2822, section 3.4.1
    * The contents of a quoted local part can not contain characters:
    *    9 (TAB)
    *    10 (LF)
    *    13 (CR)
    *    32 (space)
    *    34 (")
    *    91-94 ([, \, ], ^)
    */
   public static String INVALID_QUOTED_COMMA = "\"test,user\"@example.com";
   public static String INVALID_QUOTED_BACKSLASH = "\"test\\user\"@example.com";
   public static String INVALID_QUOTED_LEFT_BRACKET = "\"test[user\"@example.com";
   public static String INVALID_QUOTED_RIGHT_BRACKET = "\"test]user\"@example.com";
   public static String INVALID_QUOTED_CARAT = "\"test^user\"@example.com";
   public static String INVALID_QUOTED_SPACE = "\"test user\"@example.com";
   public static String INVALID_QUOTED_QUOTE = "\"test\"user\"@example.com";
   // TODO: public static String Invalid_quoted_tab = "test.\"".concat("\t").concat("\".user@example.com");

   /*
    * RFC 2822, section 3.4.1
    * If the quoted local part has a backslash, the following character is escaped and must not be 10 (LF), 13 (CR).
    */
   public static String INVALID_QUOTED_RETURN = "test.\"\\".concat("\r").concat("\".user@example.com");
   public static String INVALID_QUOTED_LINEFEED = "test.\"\\".concat("\n").concat("\".user@example.com");

   /*
    * RFC 1035, section 2.3.4
    * A plain domain consists of labels separated with periods. No period can start or end a domain name.
    * No two periods in succession can be in a domain name.
    */
   public static String TRAILING_DOMAIN_DOT = "email@example.com.";
   public static String LEADING_DOMAIN_DOT = "email@.example.com";
   public static String SUCCESSIVE_DOMAIN_DOTS = "email@example..com";

   /*
    * RFC 2822, section 3.4.1
    * Bracketed domains must:
    *    start with [, end with ]
    *    not contain characters 9 (TAB), 10 (LF), 13 (CR), 32 (space), 91-94 ([, \, ], ^)
    */
   public static String INCORRECTLY_BRACKETED_DOMAIN = "email@[example].com";
   public static String INVALID_DOMAIN_CHARACTER = "email@[ex^ample.com]";
   public static String INCORRECTLY_ESCAPED_DOMAIN = "email@[exa\\mple.com]";

   /*
    * RFC 1035, section 2.3.4
    * The maximum length of a label is 63 characters.
    */
   public static String DOMAIN_LABEL_LENGTH_EXCEEDED =
      "email@IJUr9P6Y7Fx7rFy4sziQDT0qvSC7XKK6jrD0CNC41jorAKgFYIXLTN5ITJLohy58.com";

   /*
    * RFC 1035, section 2.3.4
    * A label may contain hyphens, but no two hyphens in a row.
    * A label must not start nor end with a hyphen.
    */
   public static String LEADING_DASH_DOMAIN = "email@-example.com";
   public static String TRAILING_DASH_DOMAIN = "email@example-.com";
   public static String MULTIPLE_DASHES_DOMAIN = "email@exa--mple.com";
   public static String LEADING_DASH_BRACKETED_DOMAIN = "email@[-example.com]";
   public static String TRAILING_DASH_BRACKETED_DOMAIN = "email@[example.com-]";
   public static String MULTIPLE_DASHES_BRACKETED_DOMAIN = "email@[exa--mple.com]";

      /*
       * The contents of a bracketed domain can have a \ precede a character to escape it, and the following character
       * must not be 10 (LF) or 13 (CR).
       */
   public static String INVALID_BRACKETED_DOMAIN_RETURN = "test@[\\".concat("\r").concat("example.com]");
   public static String INVALID_BRACKETED_DOMAIN_LINEFEED = "test@[\\".concat("\n").concat("example.com]");

   /*
    * RFC 2821, section 4.5.3.1
    * The maximum length of the local part is 64 characters.
    */
   public static String LOCALPART_LENGTH_EXCEEDED =
      "emailuhpealgyxntsh5upl5gqn5a4ruqs7mw6wz21j6dn72amzwozqlyua4jx16rd@example.com";

   /*
    * RFC 3696, section 2
    * The top level domain must be all alphabetic.
    */
   public static String INVALID_ENCODED_HTML = "Joe Smith <email@example.com>";
   public static String INVALID_FOLLOWING_TEXT = "email@example.com (Joe Smith)";
   public static String INVALID_IP_FORMAT = "email@111.222.333.44444";

   /*
    * RFC 2821, section 4.5.3.1
    * The maximum length of a "useful" email address is 255 characters.
    */
   public static String MAX_EMAIL_LENGTH_EXCEEDED =
      "email@"+
      "Hk3yhCtbBRw3wCT76tL1ryAdfrIaaDszHqvZqnNrZPlNn3Wd7u."+
      "RfpxrueSghp9dkGTGwT9s0fyJL850Sned72RD3Mm5PpEh6QJwQ."+
      "3CeXyEHQEhXNOQdWhYVjGBLzlHz1sJfi4lfn7ighLXcxa5cMAK."+
      "jFXsG8BVsvkODKktTXJ70bQmDWtWQzuh3oz4twumVArDGEbzS1."+
      "slyaBcQqVgUdqXTBdbMY7YJxZwrzZQBBGjCl4e.com";
}
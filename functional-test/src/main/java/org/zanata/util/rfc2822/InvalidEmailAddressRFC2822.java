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
package org.zanata.util.rfc2822;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2822.txt">RFC2822 Internet Message Format Standard</a>
 * Synopsis:
 * This enumeration represents a collection of invalid email addresses, as stipulated in the
 * RFC2822 Internet Message Format standard, or referred to standards.
 *
 * Definitions
 * localpart: the section of an address preceding the @ symbol
 * domain: the section of an address following the @ symbol
 * label: section of localpart or domain between the start, @ symbol, period or
 *    end (also referred to as "atom")
 *    e.g. me, myself, example, com in me.myself@example.com
 * quote / quoting: a section of the localpart contained within quotation marks
 *
 * Untested:
 *
 * RFC 2821, section 4.5.3.1
 * The maximum length of a "useful" email address is 255 characters.
 *
 * RFC 3696
 * The maximum allowable length of an email address is 320 characters.
 */
public enum InvalidEmailAddressRFC2822 {

   /**
    * Email addresses consist of a local part, the "@" symbol, and the domain.
    * @see "RFC 2822, section 3.4.1"
    */
   PLAIN_ADDRESS("plainaddress"),
   MISSING_AMPERSAT("email.example.com"),
   MISSING_LOCALPART("@example.com"),
   MISSING_DOMAIN("email@"),
   MULTIPLE_APERSAT("email@example@example.com"),

   /**
    * No periods can start or end the local part.
    * Two periods together is invalid.
    * @see "RFC 2822, section 3.4.1"
    */
   LEADING_DOT(".email@example.com"),
   TRAILING_DOT("email.@example.com"),
   MULTIPLE_DOTS("email..email@example.com"),

   /**
    * All email addresses are in 7-bit US ASCII.
    * @see "RFC 2822, section 2.2"
    */
   NON_UNICODE_CHARACTERS("あいうえお@example.com"),

   /**
    * Unquoted local parts can consist of TEXT
    * TEXT can contain:
    *    alphabetic
    *    numeric
    *    and symbols !#$%'*+-/=?^_`{|}~
    * @see "RFC 2822, section 3.4.1"
    */
   INVALID_UNQUOTED_COMMA("test,user@example.com"),
   INVALID_UNQUOTED_LEFT_PARENTHESES("test(user@example.com"),
   INVALID_UNQUOTED_RIGHT_PARENTHESES("test)user@example.com"),

   /**
    * The quoted local part starts with a quotation mark, ends with a quotation mark.
    * @see "RFC 2822, section 3.4.1"
    */
   INVALID_SINGLE_QUOTING("test\"user@example.com"),

   /**
    * If an email is using the obsolete quoting on a per-label basis, then the email
    * address consists of unquoted or quoted chunks separated by periods
    * @see "RFC 2822, section 4.4"
    */
   INVALID_QUOTING_SEPARATION("\"test\"user@example.com"),

   /**
    * The contents of a quoted local part can not contain characters:
    *    9 (TAB)
    *    10 (LF)
    *    13 (CR)
    *    32 (space)
    *    34 (")
    *    91-94 ([, \, ], ^)
    * @see "RFC 2822, section 3.4.1"
    */
   INVALID_QUOTED_COMMA("\"test,user\"@example.com"),
   INVALID_QUOTED_BACKSLASH("\"test\\user\"@example.com"),
   INVALID_QUOTED_LEFT_BRACKET("\"test[user\"@example.com"),
   INVALID_QUOTED_RIGHT_BRACKET("\"test]user\"@example.com"),
   INVALID_QUOTED_CARAT("\"test^user\"@example.com"),
   INVALID_QUOTED_SPACE("\"test user\"@example.com"),
   INVALID_QUOTED_QUOTE("\"test\"user\"@example.com"),
   INVALID_QUOTED_TAB("test.\"".concat("\t").concat("\".user@example.com")),

   /**
    * If the quoted local part has a backslash, the following character is escaped and must not be
    * 10 (LF), 13 (CR).
    * @see "RFC 2822, section 3.4.1"
    */
   INVALID_QUOTED_RETURN("test.\"\\".concat("\r").concat("\".user@example.com")),
   INVALID_QUOTED_LINEFEED("test.\"\\".concat("\n").concat("\".user@example.com")),

   /**
    * A plain domain consists of labels separated with periods. No period can start or end a
    * domain name. No two periods in succession can be in a domain name.
    * @see "RFC 1035, section 2.3.4"
    */
   TRAILING_DOMAIN_DOT("email@example.com."),
   LEADING_DOMAIN_DOT("email@.example.com"),
   SUCCESSIVE_DOMAIN_DOTS("email@example..com"),

   /**
    * Bracketed domains must:
    *    start with [, end with ]
    *    not contain characters 9 (TAB), 10 (LF), 13 (CR), 32 (space), 91-94 ([, \, ], ^)
    * @see "RFC 2822, section 3.4.1"
    */
   INCORRECTLY_BRACKETED_DOMAIN("email@[example].com"),
   INVALID_DOMAIN_CHARACTER("email@[ex^ample.com]"),
   INCORRECTLY_ESCAPED_DOMAIN("email@[exa\\mple.com]"),

   /**
    * The maximum length of a label is 63 characters.
    * @see "RFC 1035, section 2.3.4"
    */
   DOMAIN_LABEL_LENGTH_EXCEEDED("email@IJUr9P6Y7Fx7rFy4sziQDT0qvSC7XKK6jrD0CNC41jorAKgFYIXLTN5ITJLohy58.com"),

   /**
    * A label may contain hyphens, but no two hyphens in a row.
    * A label must not start nor end with a hyphen.
    * @see "RFC 1035, section 2.3.4"
    */
   LEADING_DASH_DOMAIN("email@-example.com"),
   TRAILING_DASH_DOMAIN("email@example-.com"),
   MULTIPLE_DASHES_DOMAIN("email@exa--mple.com"),
   LEADING_DASH_BRACKETED_DOMAIN("email@[-example.com]"),
   TRAILING_DASH_BRACKETED_DOMAIN("email@[example.com-]"),
   MULTIPLE_DASHES_BRACKETED_DOMAIN("email@[exa--mple.com]"),

   /**
    * The contents of a bracketed domain can have a \ precede a character to escape it, and the
    * following character must not be 10 (LF) or 13 (CR).
    * @see "RFC2822, section 3.4.1"
    */
   INVALID_BRACKETED_DOMAIN_RETURN("test@[\\".concat("\r").concat("example.com]")),
   INVALID_BRACKETED_DOMAIN_LINEFEED("test@[\\".concat("\n").concat("example.com]")),

   /**
    * The maximum length of the local part is 64 characters.
    * @see "RFC 2821, section 4.5.3.1"
    */
   LOCALPART_LENGTH_EXCEEDED("emailuhpealgyxntsh5upl5gqn5a4ruqs7mw6wz21j6dn72amzwozqlyua4jx16rd@example.com"),

   /**
    * The top level domain must be all alphabetic.
    * @see "RFC 3696, section 2"
    */
   INVALID_ENCODED_HTML("Joe Smith <email@example.com>"),
   INVALID_FOLLOWING_TEXT("email@example.com (Joe Smith)"),
   INVALID_IP_FORMAT("email@111.222.333.44444"),

   /**
    * The maximum length of a "useful" email address is 255 characters.
    * @see "RFC 2821, section 4.5.3.1"
    */
   MAX_EMAIL_LENGTH_EXCEEDED("email@"+
      "Hk3yhCtbBRw3wCT76tL1ryAdfrIaaDszHqvZqnNrZPlNn3Wd7u."+
      "RfpxrueSghp9dkGTGwT9s0fyJL850Sned72RD3Mm5PpEh6QJwQ."+
      "3CeXyEHQEhXNOQdWhYVjGBLzlHz1sJfi4lfn7ighLXcxa5cMAK."+
      "jFXsG8BVsvkODKktTXJ70bQmDWtWQzuh3oz4twumVArDGEbzS1."+
      "slyaBcQqVgUdqXTBdbMY7YJxZwrzZQBBGjCl4e.com");

   private final String address;

   private InvalidEmailAddressRFC2822(String address)
   {
      this.address = address;
   }

   public String toString()
   {
      return address;
   }
}
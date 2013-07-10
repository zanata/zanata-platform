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
    * BUG982048
    * This defect is an list of all items that, valid or invalid, are not correctly recognised by the email validation
    * in Zanata.
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

   public static Map<String, String> invalidEmailAddresses()
   {
      Map<String, String> invalidEmailAddresses = new HashMap<String, String>();

      /*
       * RFC 2822, section 3.4.1
       * Email addresses consist of a local part, the "@" symbol, and the domain.
       */
      invalidEmailAddresses.put("3.4.1 Plain address", "plainaddress");
      invalidEmailAddresses.put("3.4.1 Missing @", "email.example.com");
      invalidEmailAddresses.put("3.4.1 Missing localpart", "@example.com");
      invalidEmailAddresses.put("3.4.1 Missing domain", "email@");
      invalidEmailAddresses.put("3.4.1 Two @ sign", "email@example@example.com");

      /*
       * RFC 2822, section 3.4.1
       * No periods can start or end the local part.
       * Two periods together is invalid.
       */
      invalidEmailAddresses.put("3.4.1 Leading dot", ".email@example.com");
      invalidEmailAddresses.put("3.4.1 Trailing dot", "email.@example.com");
      invalidEmailAddresses.put("3.4.1 Multiple dots", "email..email@example.com");

      /*
       * RFC 2822, section 2.2
       * All email addresses are in 7-bit US ASCII.
       */
      // BUG982048invalidEmailAddresses.put("3.4.1 Non unicode characters", "あいうえお@example.com");

      /*
       * RFC 2822, section 3.4.1
       * Unquoted local parts can consist of TEXT
       * TEXT can contain:
       *    alphabetic
       *    numeric
       *    and symbols !#$%'*+-/=?^_`{|}~
       */
      invalidEmailAddresses.put("3.4.1 Invalid unquoted character", "test,user@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid unquoted character", "test(user@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid unquoted character", "test)user@example.com");

      /*
       * RFC 2822, section 3.4.1
       * The quoted local part starts with a quotation mark, ends with a quotation mark.
       */
      invalidEmailAddresses.put("3.4.1 Invalid quoting", "test\"user@example.com");

      /*
       * RFC 2822, section 4.4
       * If an email is using the obsolete quoting on a per-label basis, then the email address consists of unquoted
       * or quoted chunks separated by periods
       */
      invalidEmailAddresses.put("3.4.1 Invalid quoting", "\"test\"user@example.com");
      invalidEmailAddresses.put("4.4 Invalid quoting", "\"test\"quote\"user@example.com");

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
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test,user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test\\user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test[user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test]user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test^user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "\"test\"user\"@example.com");
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "test.\"".concat("\t").concat("\".user@example.com"));

      /*
       * RFC 2822, section 3.4.1
       * If the quoted local part has a backslash, the following character is escaped and must not be 10 (LF), 13 (CR).
       */
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "test.\"\\".concat("\r").concat("\".user@example.com"));
      invalidEmailAddresses.put("3.4.1 Invalid quoted character", "test.\"\\".concat("\n").concat("\".user@example.com"));

      /*
       * RFC 1035, section 2.3.4
       * A plain domain consists of labels separated with periods. No period can start or end a domain name.
       * No two periods in succession can be in a domain name.
       */
      invalidEmailAddresses.put("RFC1035-2.3.4 Trailing dot in domain", "email@example.com.");
      invalidEmailAddresses.put("RFC1035-2.3.4 Leading dot in domain", "email@.example.com");
      invalidEmailAddresses.put("RFC1035-2.3.4 Multiple dots in domain", "email@example..com");

      /*
       * RFC 2822, section 3.4.1
       * Bracketed domains must:
       *    start with [, end with ]
       *    not contain characters 9 (TAB), 10 (LF), 13 (CR), 32 (space), 91-94 ([, \, ], ^)
       */
      invalidEmailAddresses.put("3.4.1 Incorrectly quoted domain", "email@[example].com");
      invalidEmailAddresses.put("3.4.1 Incorrectly quoted domain", "email@[ex^ample.com]");
      invalidEmailAddresses.put("3.4.1 Incorrectly quoted domain", "email@[exa\\mple].com");

      /*
       * RFC 1035, section 2.3.4
       * The maximum length of a label is 63 characters.
       */
      // BUG982048 invalidEmailAddresses.put("RFC1035-2.3.4 Domain label too long",
      //      "email@IJUr9P6Y7Fx7rFy4sziQDT0qvSC7XKK6jrD0CNC41jorAKgFYIXLTN5ITJLohy58.com");

      /*
       * RFC 1035, section 2.3.4
       * A label may contain hyphens, but no two hyphens in a row.
       * A label must not start nor end with a hyphen.
       */
      // BUG982048 invalidEmailAddresses.put("2.3.4 Leading dash in domain", "email@-example.com");
      // BUG982048 invalidEmailAddresses.put("2.3.4 Trailing dash in domain", "email@example-.com");
      // BUG982048 invalidEmailAddresses.put("2.3.4 Multiple dashes in domain", "email@exa--mple.com");
      invalidEmailAddresses.put("2.3.4 Leading dash in bracketed domain", "email@[-example.com]");
      invalidEmailAddresses.put("2.3.4 Trailing dash in bracketed domain", "email@[example.com-]");
      invalidEmailAddresses.put("2.3.4 Multiple dashes in bracketed domain", "email@[exa--mple.com]");

      /*
       * The contents of a bracketed domain can have a \ precede a character to escape it, and the following character
       * must not be 10 (LF) or 13 (CR).
       */
      invalidEmailAddresses.put("3.4.1 Invalid bracketed domain", "test@[\\".concat("\r").concat("example.com]"));
      invalidEmailAddresses.put("3.4.1 Invalid bracketed domain", "test@[\\".concat("\n").concat("example.com]"));

      /*
       * RFC 2821, section 4.5.3.1
       * The maximum length of the local part is 64 characters.
       */
      invalidEmailAddresses.put("RFC2821-4.5.3.1 Max localpart length is 64",
            "emailuhpealgyxntsh5upl5gqn5a4ruqs7mw6wz21j6dn72amzwozqlyua4jx16rd@example.com");

      /*
       * RFC 3696, section 2
       * The top level domain must be all alphabetic.
       */
      invalidEmailAddresses.put("RFC3696-2 Encoded html", "Joe Smith <email@example.com>");
      invalidEmailAddresses.put("RFC3696-2 Following text", "email@example.com (Joe Smith)");
      // BUG982048 invalidEmailAddresses.put("RFC3696-2 Invalid IP", "email@111.222.333.44444");

      /*
       * RFC 2821, section 4.5.3.1
       * The maximum length of a "useful" email address is 255 characters.
       */
      /*
       * BUG982048
      invalidEmailAddresses.put("4.5.3.1 Max email length is 255",
            "email@"+
            "Hk3yhCtbBRw3wCT76tL1ryAdfrIaaDszHqvZqnNrZPlNn3Wd7u."+
            "RfpxrueSghp9dkGTGwT9s0fyJL850Sned72RD3Mm5PpEh6QJwQ."+
            "3CeXyEHQEhXNOQdWhYVjGBLzlHz1sJfi4lfn7ighLXcxa5cMAK."+
            "jFXsG8BVsvkODKktTXJ70bQmDWtWQzuh3oz4twumVArDGEbzS1."+
            "slyaBcQqVgUdqXTBdbMY7YJxZwrzZQBBGjCl4e.com");
       */
      return invalidEmailAddresses;
   }

   /*
    * An map of valid emails conforming to RFC2822
    */
   public static Map<String, String> validEmailAddresses()
   {
      Map<String, String> validEmailAddresses = new HashMap<String, String>();

      /*
       * RFC 2822, section 3.4.1
       * Email addresses consist of a local part, the "@" symbol, and the domain.
       */
      validEmailAddresses.put("3.4.1 Basic email", "email@example.com");

      /*
       * RFC 2822, sections 3.4.1 and 4.4
       * The local part can be unquoted, quoted in its entirety, or quoted on a per-label basis.
       * The quoted local part starts with a quotation mark, ends with a quotation mark.
       */
      // BUG982048 validEmailAddresses.put("3.4.1 Basic quoted email", "\"email\"@example.com");

      /*
       * RFC 2822, section 3.4.1
       * TEXT can contain alphabetic, numeric, and these symbols: !#$%'*+-/=?^_`{|}~
       */
      validEmailAddresses.put("3.4.1 Allowed special characters in localpart", "email.!#$%'*+-/=?^_`{|}~.dot@example.com");

      /*
         RFC 2822, section 4.4
         If an email is using the obsolete quoting on a per-label basis, then the email address consists of unquoted
         or quoted chunks separated by periods.
       */
      // BUG982048 validEmailAddresses.put("4.4 Quoted label with surrounding labels", "dot.\"email\".dot@example.com");
      // BUG982048 validEmailAddresses.put("4.4 Localpart with empty quote", "dot.\"\".dot@example.com");

      /*
       * RFC 2822, section 3.4.1
       * If the quoted local part has a backslash, the following character is escaped and must not be 10 (LF), 13 (CR).
       * This supersedes the previous rule, allowing spaces and quotation marks in the email address as long as they
       * are escaped.
       */
      // BUG982048 validEmailAddresses.put("3.4.1 Quoted email with escaped special characters", "email.\"(),:;<>\\@\\[\\]\\\\\"@example.com");
      // BUG982048 validEmailAddresses.put("3.4.1 Quoted email with escaped quotes", "email.\"\\\"\"@example.com");
      // BUG982048 validEmailAddresses.put("3.4.1 Quoted email with space character", "\"special\\ email\"@example.com");

      /*
       * RFC 2822, section 3.4.1
       * The domain can be bracketed or plain.
       */
      // BUG982048 validEmailAddresses.put("3.4.1 Email with bracketed domain", "email@[example.com]");
      // BUG982048 validEmailAddresses.put("3.4.1 Bracketed IPv6 domain", "email@[123.45.67.89]");
      // BUG982048 validEmailAddresses.put("3.4.1 Bracketed IPv6 domain", "email@[IPv6:2001:2d12:c4fe:5afe::1]");

      /*
       * RFC 1035, section 2.3.4
       * A plain domain consists of labels separated with periods. No period can start or end a domain name.
       */
      validEmailAddresses.put("RFC1035-2.3.4 Localpart with multiple labels", "another.email@example.com");
      validEmailAddresses.put("RFC1035-2.3.4 Domain with multiple labels", "email@another.example.com");

      /*
       * RFC 1035, section 2.3.4
       * The maximum length of a label is 63 characters.
       */
      validEmailAddresses.put("RFC1035-2.3.4 Domain label of 63 characters",
            "email@B3NQyUsDdzODMoymfDdifn6Wztx2wrivm80LEngHGl182frm6ifCPyv5SntbDg8.com");
      validEmailAddresses.put("RFC1035-2.3.4 Localpart label of 63 characters",
            "B3NQyUsDdzODMoymfDdifn6Wztx2wrivm80LEngHGl182frm6ifCPyv5SntbDg8@example.com");

      /*
       * RFC 1035, section 2.3.4
       * A label may contain hyphens, but no two hyphens in a row.
       */
      validEmailAddresses.put("RFC1035-2.3.4 Hyphenated domain label", "email@another-example.com");
      validEmailAddresses.put("RFC1035-2.3.4 Hyphenated localpart label", "my-email@example.com");

      /*
       * RFC 2821, section 4.5.3.1
       * The maximum length of the local part is 64 characters.
       */
      validEmailAddresses.put("RFC1035-2.3.4 Localpart length of 64 characters",
            "B3NQyUsDdzODMoymfDdifn6Wztx2wrivm.80LEngHGl182frm6ifCPyv5SntbDg8@example.com");

      return validEmailAddresses;
   }
}
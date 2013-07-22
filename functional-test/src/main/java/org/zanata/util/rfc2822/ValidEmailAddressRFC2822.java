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
 * This enumeration represents a collection of valid email addresses, as stipulated in the
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
public enum ValidEmailAddressRFC2822 {

   /**
    * Email addresses consist of a local part, the "@" symbol, and the domain.
    * @see "RFC 2822, section 3.4.1"
    */
   BASIC_EMAIL("email@example.com"),

   /**
    * The local part can be unquoted, quoted in its entirety, or quoted on a per-label basis.
    * The quoted local part starts with a quotation mark, ends with a quotation mark.
    * @see "RFC 2822, sections 3.4.1 and 4.4"
    */
   BASIC_QUOTED_EMAIL("\"email\"@example.com"),

   /**
    * TEXT can contain alphabetic, numeric, and these symbols: !#$%'*+-/=?^_`{|}~
    * @see "RFC 2822, section 3.4.1"
    */
   SPECIAL_CHARACTERS_LOCALPART("email.!#$%'*+-/=?^_`{|}~.dot@example.com"),

   /**
    * If an email is using the obsolete quoting on a per-label basis, then the email address
    * consists of unquoted or quoted chunks separated by periods.
    * @see "RFC 2822, section 4.4"
    */
   ENCLOSED_QUOTED_LABEL("dot.\"email\".dot@example.com"),
   LOCALPART_WITH_EMPTY_QUOTE("dot.\"\".dot@example.com"),

   /**
    * If the quoted local part has a backslash, the following character is escaped and must not
    * be 10 (LF), 13 (CR).
    * This supersedes the previous rule, allowing spaces and quotation marks in the email address
    * as long as they are escaped.
    * @see "RFC 2822, section 3.4.1"
    */
   QUOTED_ESCAPED_SPECIAL_CHARACTERS("email.\"(),:;<>\\@\\[\\]\\\\\"@example.com"),
   QUOTED_ESCAPED_QUOTES("email.\"\\\"\"@example.com"),
   QUOTED_WITH_SPACE("\"special\\ email\"@example.com"),

   /**
    * The domain can be bracketed or plain.
    * @see "RFC 2822, section 3.4.1"
    */
   BRACKETED_DOMAIN("email@[example.com]"),
   BRACKETED_IPV4_DOMAIN("email@[123.45.67.89]"),
   BRACKETED_IPV6_DOMAIN("email@[IPv6:2001:2d12:c4fe:5afe::1]"),

   /**
    * A plain domain consists of labels separated with periods. No period can start or end
    * a domain name.
    * @see "RFC 1035, section 2.3.4"
    */
   LOCALPART_MULTIPLE_LABELS("another.email@example.com"),
   DOMAIN_MULTIPLE_LABELS("email@another.example.com"),

   /**
    * The maximum length of a label is 63 characters.
    * @see "RFC 1035, section 2.3.4"
    */
   DOMAIN_LABEL_MAX_CHARACTERS("email@B3NQyUsDdzODMoymfDdifn6Wztx2wrivm80LEngHGl182frm6ifCPyv5SntbDg8.com"),
   LOCALPART_LABEL_MAX_CHARACTERS("B3NQyUsDdzODMoymfDdifn6Wztx2wrivm80LEngHGl182frm6ifCPyv5SntbDg8@example.com"),

   /**
    * A label may contain hyphens, but no two hyphens in a row.
    * @see "RFC 1035, section 2.3.4"
    */
   HYPHENATED_DOMAIN_LABEL("email@another-example.com"),
   HYPHENATED_LOCALPART_LABEL("my-email@example.com"),

   /**
    * The maximum length of the local part is 64 characters.
    * @see "RFC 2821, section 4.5.3.1"
    */
   LOCALPART_MAX_LENGTH("B3NQyUsDdzODMoymfDdifn6Wztx2wrivm.80LEngHGl182frm6ifCPyv5SntbDg8@example.com");

   private final String address;

   private ValidEmailAddressRFC2822(String address)
   {
      this.address = address;
   }

   public String toString()
   {
      return address;
   }
}
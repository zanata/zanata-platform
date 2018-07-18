/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.annotation.Nonnull;

import net.sf.okapi.common.exceptions.OkapiIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.steps.tokenization.Tokenizer;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class OkapiUtil {
    private static final Logger log = LoggerFactory.getLogger(OkapiUtil.class);

    private OkapiUtil() {
    }

    /**
     * Validate a Zanata localeId to BCP-47 and return an equivalent Okapi LocaleId
     * @param zanataLocale Zanata LocaleId to convert
     * @return Okapi LocaleId, as-is to preserve case (no normalization)
     * @throws IllegalArgumentException on validation failure
     */
    public static @Nonnull
    LocaleId toOkapiLocale(@Nonnull org.zanata.common.LocaleId zanataLocale)
            throws IllegalArgumentException {
        LocaleId.fromBCP47(zanataLocale.getId());
        return new LocaleId(zanataLocale.getId(), false);
    }

    /**
     * Count words using Okapi's WordCounter, which tries to implement the LISA
     * standard <a href=
     * "http://web.archive.org/web/20090403134742/http://www.lisa.org/Global-information-m.105.0.html"
     * >GMX-V</a>
     *
     * @param s
     * @param bcp47Locale
     * @return
     */
    public static long countWords(String s, String bcp47Locale) {
        if (s == null) {
            log.debug("null string");
            return 0;
        }
        try {
            LocaleId locale;
            try {
                locale = LocaleId.fromBCP47(bcp47Locale);
            } catch (Exception e) {
                log.error(
                        "can't understand '{}' as a BCP-47 locale; defaulting to English",
                        bcp47Locale);
                locale = LocaleId.ENGLISH;
            }

            Tokens tokens = StringTokenizer.tokenizeString(s, locale, "WORD");
            return tokens.size();
        } catch (Exception e) {
            Object[] args = new Object[] { s, bcp47Locale, e };
            log.error("unable to count words in string '{}' for locale '{}'",
                    args);
            return 0;
        }
    }

    private static class StringTokenizer extends Tokenizer {
        public static Tokens tokenizeString(String text, LocaleId language,
                String... tokenNames) {
            synchronized (Tokenizer.class) {
                return Tokenizer.tokenizeString(text, language, tokenNames);
            }
        }
    }

}

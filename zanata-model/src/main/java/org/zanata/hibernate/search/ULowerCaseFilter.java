package org.zanata.hibernate.search;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.ibm.icu.lang.UCharacter;

/**
 * Uses ICU case folding to convert tokens to lowercase.
 *
 * @author David Mason, damason@redhat.com
 */
public class ULowerCaseFilter extends TokenFilter {

    private CharTermAttribute termText;

    public ULowerCaseFilter(TokenStream input) {
        super(input);
        termText = addAttribute(CharTermAttribute.class);
    }

    public final boolean incrementToken() throws IOException {
        boolean hasToken = input.incrementToken();
        if (hasToken) {
            final char[] buffer = termText.buffer();
            final int length = termText.length();
            for (int i = 0; i < length; i++) {
                buffer[i] = (char) UCharacter.foldCase(buffer[i], true);
            }
        }
        return hasToken;
    }
}

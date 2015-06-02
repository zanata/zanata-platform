/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.hibernate.search;

import java.io.Reader;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class CaseInsensitiveWhitespaceAnalyzer extends ReusableAnalyzerBase {

    private final Version matchVersion;

    /**
     * Creates a new {@link CaseInsensitiveWhitespaceAnalyzer}
     * @param matchVersion Lucene version to match See {@link <a href="#version">above</a>}
     */
    public CaseInsensitiveWhitespaceAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    @Override
    protected TokenStreamComponents createComponents(
        String fieldName, Reader reader) {

        final WhitespaceTokenizer src = new WhitespaceTokenizer(matchVersion, reader);
        TokenStream tok = new LowerCaseFilter(matchVersion, src);

        return new TokenStreamComponents(src, tok);
    }
}

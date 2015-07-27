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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * Analyzer that tokenises into lower-case unigrams.
 * <p>
 * NOTE: this class is currently not used at runtime by Hibernate Search.
 * There may be differences between this class's behaviour and that of the
 * Analyzer used at runtime.
 * </p>
 * @see org.zanata.model.HTextContainer
 */
public final class UnigramAnalyzer extends Analyzer {

    private final Version matchVersion;

    /**
     *
     * @param matchVersion Lucene version to match
     */
    public UnigramAnalyzer(Version matchVersion) {
        this.matchVersion = matchVersion;
    }

    /**
     * IMPORTANT: make sure this matches the AnalyzerDef in
     * {@link org.zanata.model.HTextContainer}.
     */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        NGramTokenizer source = new NGramTokenizer(reader, 1, 1);
        if (fieldName != null && fieldName.contains("content-nocase")) {
            // TODO should we be using ULowerCaseFilter (also in HTextContainer)?
            return new LowerCaseFilter(matchVersion, source);
        } else {
            return source;
        }
    }
}

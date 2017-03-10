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
package org.zanata.hibernate.search;

import java.io.IOException;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;
import org.zanata.common.LocaleId;
import static org.apache.lucene.search.DocIdSetIterator.NO_MORE_DOCS;

public class LocaleFilter extends Filter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LocaleFilter.class);
    private static final long serialVersionUID = 1L;
    private LocaleId locale;

    public LocaleFilter(LocaleId locale) {
        this.locale = locale;
    }

    @Override
    public String toString(String field) {
        return "LocaleFilter(locale=" + locale + ")";
    }

    @Override
    public DocIdSet getDocIdSet(LeafReaderContext context, Bits acceptDocs)
            throws IOException {
        log.debug("getDocIdSet for {}", locale);
        LeafReader reader = context.reader();
        Term term = new Term("locale", locale.toString());
        return liveDocsBitSet(reader, term);
    }

    private static DocIdSet liveDocsBitSet(LeafReader reader, Term term)
            throws IOException {
        FixedBitSet bitSet = new FixedBitSet(reader.maxDoc());
        Bits liveDocs = reader.getLiveDocs();
        PostingsEnum termDocs = reader.postings(term);
        long setSize = 0;
        while (termDocs.nextDoc() != NO_MORE_DOCS) {
            int docID = termDocs.docID();
            if (liveDocs == null || liveDocs.get(docID)) {
                bitSet.set(docID);
                ++setSize;
                // else document is deleted...
            }
        }
        return new BitDocIdSet(bitSet, setSize);
    }
}

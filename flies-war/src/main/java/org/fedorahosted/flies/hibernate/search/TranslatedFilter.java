package org.fedorahosted.flies.hibernate.search;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.DocIdBitSet;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.TextFlowDAO;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("translatedFilter")
public class TranslatedFilter extends Filter {
	
	@In
	TextFlowDAO textFlowDAO;
	
	private LocaleId locale;
	
	public LocaleId getLocale() {
		return locale;
	}
	
	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}
	
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		BitSet bitSet = new BitSet(reader.maxDoc());
		
		List<Long> translatedIds = textFlowDAO.getIdsByTargetState(locale, ContentState.Approved);
		for (Long tfId : translatedIds) {
			Term term = new Term("id", tfId.toString());
			TermDocs termDocs = reader.termDocs(term);
			while (termDocs.next())
				bitSet.set(termDocs.doc());
		}
		return new DocIdBitSet(bitSet);
	}
}

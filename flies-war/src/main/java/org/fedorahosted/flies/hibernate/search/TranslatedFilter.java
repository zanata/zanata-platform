package org.fedorahosted.flies.hibernate.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.dao.TextFlowDAO;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("translatedFilter")
public class TranslatedFilter extends Filter
{

   @In
   TextFlowDAO textFlowDAO;

   private LocaleId locale;

   @Logger
   Log log;

   public LocaleId getLocale()
   {
      return locale;
   }

   public void setLocale(LocaleId locale)
   {
      log.debug("Setting locale to {0}", locale);
      this.locale = locale;
   }

   @Override
   public DocIdSet getDocIdSet(IndexReader reader) throws IOException
   {
      OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());

      List<Long> translatedIds = textFlowDAO.getIdsByTargetState(locale, ContentState.Approved);
      log.debug("{0} matching TF ids for locale {0}: {1}", translatedIds.size(), locale, translatedIds);
      for (Long tfId : translatedIds)
      {
         Term term = new Term("id", tfId.toString());
         TermDocs termDocs = reader.termDocs(term);
         while (termDocs.next())
            bitSet.set(termDocs.doc());
      }
      return bitSet;
   }
}

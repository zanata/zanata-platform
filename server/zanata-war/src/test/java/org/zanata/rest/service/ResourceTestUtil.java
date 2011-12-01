package org.zanata.rest.service;

import java.util.Iterator;
import java.util.List;

import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.TranslationsResourceExtension;
import org.zanata.rest.dto.resource.AbstractResourceMeta;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;


class ResourceTestUtil
{

   static void clearRevs(AbstractResourceMeta doc)
   {
      doc.setRevision(null);
   
      if (doc instanceof Resource)
      {
         Resource res = (Resource) doc;
         final List<TextFlow> textFlows = res.getTextFlows();
         if (textFlows != null)
            for (TextFlow tf : textFlows)
            {
               tf.setRevision(null);
            }
      }
   
   }

   static void clearRevs(TranslationsResource doc)
   {
      doc.setRevision(null);
      for (TextFlowTarget tft : doc.getTextFlowTargets())
      {
         tft.setRevision(null);
         tft.setTextFlowRevision(null);
      }
   }
   
   static void clearPoTargetHeaders(TranslationsResource ... docs)
   {
      for( TranslationsResource doc : docs )
      {
         Iterator<TranslationsResourceExtension> it = doc.getExtensions(true).iterator();
         while( it.hasNext() )
         {
            if( it.next() instanceof PoTargetHeader )
            {
               it.remove();
            }
         }
      }
   }

}

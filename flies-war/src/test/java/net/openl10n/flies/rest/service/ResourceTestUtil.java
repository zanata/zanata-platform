package net.openl10n.flies.rest.service;

import java.util.List;

import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

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
         tft.setTextFlowRevision(null);
      }
   }

}

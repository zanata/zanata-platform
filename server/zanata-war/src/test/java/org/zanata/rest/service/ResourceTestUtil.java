package org.zanata.rest.service;

import java.util.List;

import org.fest.assertions.Assertions;
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
   
   static void assertEquals(TranslationsResource expected, TranslationsResource actual)
   {
      Assertions.assertThat( toStringIfNull(expected.getLinks()) ).isEqualTo( toStringIfNull(actual.getLinks()) );
      Assertions.assertThat(expected.getRevision()).isEqualTo(actual.getRevision());
      Assertions.assertThat(expected.getTextFlowTargets().toString()).isEqualTo(actual.getTextFlowTargets().toString());
   }
   
   /**
    * Turns an object to String even if it is null. It uses the object's toString
    * method. If the object is null, returns an empty string.
    */
   private static String toStringIfNull( Object obj )
   {
      if( obj == null )
         return "";
      
      return obj.toString();
   }

}

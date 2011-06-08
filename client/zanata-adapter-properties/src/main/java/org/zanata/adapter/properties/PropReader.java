package org.zanata.adapter.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.fedorahosted.openprops.Properties;
import org.xml.sax.InputSource;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * A PropReader. NOT THREADSAFE.
 * 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class PropReader
{

   public static final String PROP_CONTENT_TYPE = "text/plain";

   // pre: template already extracted
   public void extractTarget(TranslationsResource doc, InputSource inputSource, LocaleId localeId, ContentState contentState) throws IOException
   {
      Properties props = loadProps(inputSource);
      for (String key : props.stringPropertyNames())
      {
         String val = props.getProperty(key);
         String id = getID(key, val);
         TextFlowTarget textFlowTarget = new TextFlowTarget(id);
         textFlowTarget.setContent(val);
         // textFlowTarget.setLang(localeId);
         textFlowTarget.setState(contentState);
         String comment = props.getComment(key);
         if (comment != null && comment.length() != 0)
         {
            SimpleComment simpleComment = textFlowTarget.getExtensions(true).findOrAddByType(SimpleComment.class);
            simpleComment.setValue(comment);
         }
         doc.getTextFlowTargets().add(textFlowTarget);
      }
   }

   // TODO allowing Readers (via InputSource) might be a bad idea
   public void extractTemplate(Resource doc, InputSource inputSource) throws IOException
   {
      List<TextFlow> resources = doc.getTextFlows();
      Properties props = loadProps(inputSource);
      for (String key : props.stringPropertyNames())
      {
         String val = props.getProperty(key);
         String id = getID(key, val);
         TextFlow textFlow = new TextFlow(id);
         textFlow.setContent(val);
         String comment = props.getComment(key);
         if (comment != null && comment.length() != 0)
         {
            SimpleComment simpleComment = textFlow.getExtensions(true).findOrAddByType(SimpleComment.class);
            simpleComment.setValue(comment);
         }
         // textFlow.setLang(LocaleId.EN);
         resources.add(textFlow);
      }
   }

   private String getID(String key, String val)
   {
      return key;
   }

   private Properties loadProps(InputSource inputSource) throws IOException
   {
      Properties props = new Properties();
      InputStream byteStream = inputSource.getByteStream();
      // NB unlike SAX, we prefer the bytestream over the charstream
      if (byteStream != null)
      {
         props.load(byteStream);
      }
      else
      {
         Reader reader = inputSource.getCharacterStream();
         props.load(reader);
      }
      return props;
   }

}

package org.fedorahosted.flies.adapter.properties;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.dto.deprecated.Document;
import org.fedorahosted.flies.rest.dto.deprecated.TextFlow;
import org.fedorahosted.flies.rest.dto.deprecated.TextFlowTarget;
import org.fedorahosted.openprops.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * A PropReader. NOT THREADSAFE.
 * 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class PropReader
{

   public static final String PROP_CONTENT_TYPE = "text/plain";

   private static final Logger log = LoggerFactory.getLogger(PropReader.class);

   public void extractAll(Document doc, File basePropertiesFile, String[] locales, ContentState contentState) throws IOException
   {
      InputStream baseInput = new BufferedInputStream(new FileInputStream(basePropertiesFile));
      try
      {
         // System.out.println("processing base " + basePropertiesFile);
         extractTemplate(doc, new InputSource(baseInput));
         for (String locale : locales)
         {
            File localeFile = new File(dropExtension(basePropertiesFile.toString()) + "_" + locale + ".properties");
            if (!localeFile.exists())
               continue;
            // System.out.println("processing " + localeFile);
            InputStream localeInput = new BufferedInputStream(new FileInputStream(localeFile));
            try
            {
               extractTarget(doc, new InputSource(localeInput), new LocaleId(locale), contentState);
            }
            finally
            {
               localeInput.close();
            }
         }
      }
      finally
      {
         baseInput.close();
      }
   }

   private String dropExtension(String f)
   {
      return f.substring(0, f.length() - ".properties".length());
   }

   // pre: template already extracted
   public void extractTarget(Document doc, InputSource inputSource, LocaleId localeId, ContentState contentState) throws IOException
   {
      Map<String, TextFlow> textFlowMap = new HashMap<String, TextFlow>();
      for (TextFlow resource : doc.getTextFlows())
      {
         textFlowMap.put(resource.getId(), resource);
      }

      Properties props = loadProps(inputSource);
      for (String key : props.stringPropertyNames())
      {
         String val = props.getProperty(key);
         String id = getID(key, val);

         TextFlow textFlow = textFlowMap.get(id);
         if (textFlow == null)
         {
            log.warn("Property with key {} in locale {} has no corresponding source in {}", new Object[] { key, localeId, doc.getId() });
            continue;
         }
         TextFlowTarget textFlowTarget = new TextFlowTarget();
         textFlowTarget.setContent(val);
         textFlowTarget.setId(id);
         textFlowTarget.setResourceRevision(textFlow.getRevision());
         textFlowTarget.setLang(localeId);
         textFlowTarget.setState(contentState);
         String comment = props.getComment(key);
         if (comment != null && comment.length() != 0)
            textFlowTarget.getOrAddComment().setValue(comment);
         textFlow.addTarget(textFlowTarget);
      }
   }

   // TODO allowing Readers (via InputSource) might be a bad idea
   public void extractTemplate(Document doc, InputSource inputSource) throws IOException
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
            textFlow.getOrAddComment().setValue(comment);
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

package org.zanata.adapter.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import org.fedorahosted.openprops.Properties;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * A Properties reader with support for skipping NON-TRANSLATABLE keys.
 * NOT THREADSAFE.
 * 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class PropReader
{
   // "8859_1" is used in Properties.java...
   private static final String ISO_8859_1 = "ISO-8859-1";
   public static final String PROP_CONTENT_TYPE = "text/plain";
   private static final String NEWLINE_REGEX = "(\r\n|\r|\n)";

   private final String charset;
   private final LocaleId sourceLocale;
   private final ContentState contentState;

   /**
    * @param charset charset to use when reading .properties files (usually "ISO-8859-1")
    * @param sourceLocale "lang" attribute for source TextFlows
    * @param contentState ContentState for new TextFlowTargets (typically Approved)
    */
   public PropReader(String charset, LocaleId sourceLocale, ContentState contentState)
   {
      this.charset = charset;
      this.sourceLocale = sourceLocale;
      this.contentState = contentState;
   }

   public PropReader()
   {
      this(ISO_8859_1, LocaleId.EN_US, ContentState.Approved);
   }

   // pre: template already extracted
   public void extractTarget(TranslationsResource doc, InputStream in) throws IOException
   {
      Properties props = loadProps(in);
      for (String key : props.keySet())
      {
         addPropEntryToDoc(doc, props, key, contentState);
      }
   }

   private void addPropEntryToDoc(TranslationsResource doc, Properties props, String key, ContentState contentState)
   {
      String content = props.getProperty(key);
      if (content == null)
         return;
      TextFlowTarget textFlowTarget = new TextFlowTarget(key);
      textFlowTarget.setContents(content);
      if (!content.isEmpty())
      {
         textFlowTarget.setState(contentState);
      }
      else
      {
         textFlowTarget.setState(ContentState.New);
      }
      String comment = props.getComment(key);
      if (comment != null && comment.length() != 0)
      {
         SimpleComment simpleComment = new SimpleComment(comment);
         textFlowTarget.getExtensions(true).add(simpleComment);
      }
      doc.getTextFlowTargets().add(textFlowTarget);
   }

   /**
    * Reads properties from a given {@link InputStream} and adds them to the
    * given {@link Resource}.
    * 
    * @param doc the resource to add properties textflows to
    * @param in the input stream to read the properties from
    * @throws IOException
    * @throws InvalidPropertiesFormatException
    */
   // TODO add documentation on exceptions thrown
   public void extractTemplate(Resource doc, InputStream in) throws IOException
   {
      List<TextFlow> resources = doc.getTextFlows();
      Properties props = loadProps(in);
      int nonTranslatableCount = 0;
      for (String key : props.stringPropertyNames())
      {
         String comment = null;
         String rawComment = props.getRawComment(key);
         if (rawComment != null && rawComment.length() != 0)
         {
            StringBuilder sb = new StringBuilder(rawComment.length());
            nonTranslatableCount = processCommentForNonTranslatable(nonTranslatableCount, rawComment, sb);
            comment = sb.toString();
         }
         if (nonTranslatableCount == 0)
         {
            String content = props.getProperty(key);
            String id = getID(key, content);
            TextFlow textFlow = new TextFlow(id, sourceLocale, content);
            if (comment != null && comment.length() != 0)
            {
               SimpleComment simpleComment = new SimpleComment(comment);
               textFlow.getExtensions(true).add(simpleComment);
            }
            resources.add(textFlow);
         }
      }
   }

   /**
    * Processes a full comment for non-translatable sections, writing
    * translatable sections to a given string buffer.
    * 
    * @param comment comment to process, may have multiple lines
    * @param sb string buffer to output comments in translatable blocks
    * @return adjusted non-translatable count, a value > 0 indicates that the
    *         current section is non-translatable
    * @throws InvalidPropertiesFormatException
    */
   private int processCommentForNonTranslatable(int nonTranslatableCount, String comment, StringBuilder sb) throws InvalidPropertiesFormatException
   {
      int nonTranslatable = nonTranslatableCount;
      String[] lines = comment.split(NEWLINE_REGEX);

      int lineNonTranslatable;
      for (String line : lines)
      {
         lineNonTranslatable = checkNonTranslatable(line);
         nonTranslatable += lineNonTranslatable;
         if (nonTranslatable < 0)
         {
            // TODO probably want a different exception here
            throw new InvalidPropertiesFormatException("Found '# END NON-TRANSLATABLE' " + "without matching '# START NON-TRANSLATABLE'");
         }
         if (nonTranslatable == 0 && lineNonTranslatable == 0)
         {
            sb.append(Properties.cookCommentLine(line));
            // TODO if not last line
            sb.append('\n');
         }
      }
      
      return nonTranslatable;
   }

   /**
    * Checks a comment for START and END of NON-TRANSLATABLE sections within a
    * single line of a comment.
    * 
    * @param line a single line of a comment
    * @return 0 if no NON-TRANSLATABLE comment is found, +1 for start, -1 for
    *         end
    */
   private int checkNonTranslatable(String line)
   {
      if (line.startsWith("# START NON-TRANSLATABLE"))
      {
         return 1;
      }
      if (line.startsWith("# END NON-TRANSLATABLE"))
      {
         return -1;
      }
      return 0;
   }

   private String getID(String key, String val)
   {
      return key;
   }

   private Properties loadProps(InputStream in) throws IOException
   {
      Reader reader = new InputStreamReader(in, charset);
      try
      {
         Properties props = new Properties();
         props.load(reader);
         return props;
      }
      finally
      {
         reader.close();
      }
   }

}

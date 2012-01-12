package org.zanata.adapter.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.fedorahosted.openprops.Properties;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PropWriter
{

   private static void logVerbose(String msg)
   {
      System.out.println(msg);
   }

   private static void makeParentDirs(File f)
   {
      File parentFile = f.getParentFile();
      if (parentFile != null)
         parentFile.mkdirs();
   }

   /**
    * Writes a properties file representation of the given {@link Resource} to
    * the given directory.
    * 
    * @param doc
    * @param baseDir
    * @throws IOException
    */
   public static void write(final Resource doc, final File baseDir, String charset) throws IOException
   {
      File baseFile = new File(baseDir, doc.getName() + ".properties");
      makeParentDirs(baseFile);

      logVerbose("Creating base file " + baseFile);
      Properties props = new Properties();
      for (TextFlow textFlow : doc.getTextFlows())
      {
         props.setProperty(textFlow.getId(), textFlow.getContent());
         SimpleComment simpleComment = textFlow.getExtensions(true).findByType(SimpleComment.class);
         if (simpleComment != null && simpleComment.getValue() != null)
            props.setComment(textFlow.getId(), simpleComment.getValue());
      }
      // props.store(System.out, null);
      Writer out = new OutputStreamWriter(new FileOutputStream(baseFile), charset);
      try
      {
         props.store(out, null);
      }
      finally
      {
         out.close();
      }
   }

   public static void write(final TranslationsResource doc, final File baseDir, String bundleName, String locale, String charset) throws IOException
   {
      Properties targetProp = new Properties();
      for (TextFlowTarget target : doc.getTextFlowTargets())
      {
         targetProp.setProperty(target.getResId(), target.getContent());
         SimpleComment simpleComment = target.getExtensions(true).findByType(SimpleComment.class);
         if (simpleComment != null && simpleComment.getValue() != null)
            targetProp.setComment(target.getResId(), simpleComment.getValue());
      }

      File langFile = new File(baseDir, bundleName + "_" + locale + ".properties");
      makeParentDirs(langFile);
      logVerbose("Creating target file " + langFile);
      // targetProp.store(System.out, null);
      Writer out2 = new OutputStreamWriter(new FileOutputStream(langFile), charset);
      try
      {
         targetProp.store(out2, null);
      }
      finally
      {
         out2.close();
      }
   }

}

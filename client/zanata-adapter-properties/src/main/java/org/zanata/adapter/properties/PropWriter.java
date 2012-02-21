package org.zanata.adapter.properties;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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

   public static void write(final Resource doc, final File baseDir) throws IOException
   {
      write(doc, baseDir, false);
   }

   public static void writeUTF8(final Resource doc, final File baseDir) throws IOException
   {
      write(doc, baseDir, true);
   }

   private static void write(final Resource doc, final File baseDir, boolean utf8) throws IOException
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
      storeProps(props, baseFile, utf8);
   }

   public static void write(final TranslationsResource doc, final File baseDir, String bundleName, String locale) throws IOException
   {
      write(doc, baseDir, bundleName, locale, false);
   }

   public static void writeUTF8(final TranslationsResource doc, final File baseDir, String bundleName, String locale) throws IOException
   {
      write(doc, baseDir, bundleName, locale, true);
   }

   private static void write(final TranslationsResource doc, final File baseDir, String bundleName, String locale, boolean utf8) throws IOException
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
      storeProps(targetProp, langFile, utf8);
   }

   private static void storeProps(Properties props, File file, boolean utf8) throws UnsupportedEncodingException, FileNotFoundException, IOException
   {
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
      try
      {
         if (utf8)
         {
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            props.store(writer, null);
         }
         else
         {
            props.store(out, null);
         }
      }
      finally
      {
         out.close();
      }
   }

}

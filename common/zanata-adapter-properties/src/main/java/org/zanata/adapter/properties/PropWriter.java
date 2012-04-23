package org.zanata.adapter.properties;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.openprops.Properties;
import org.zanata.common.ContentState;
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
         List<String> contents = textFlow.getContents();
         if (contents.size() != 1)
         {
            throw new RuntimeException("file format does not support plural forms: resId=" + textFlow.getId());
         }
         props.setProperty(textFlow.getId(), textFlow.getContents().get(0));
         SimpleComment simpleComment = textFlow.getExtensions(true).findByType(SimpleComment.class);
         if (simpleComment != null && simpleComment.getValue() != null)
            props.setComment(textFlow.getId(), simpleComment.getValue());
      }
      // props.store(System.out, null);
      storeProps(props, baseFile, utf8);
   }

   public static void write(Resource srcDoc, final TranslationsResource doc, final File baseDir, String bundleName, String locale, boolean createSkeletons) throws IOException
   {
      write(srcDoc, doc, baseDir, bundleName, locale, false, createSkeletons);
   }

   public static void writeUTF8(Resource srcDoc, final TranslationsResource doc, final File baseDir, String bundleName, String locale, boolean createSkeletons) throws IOException
   {
      write(srcDoc, doc, baseDir, bundleName, locale, true, createSkeletons);
   }

   private static void write(
         Resource srcDoc, final TranslationsResource doc,
         final File baseDir, String bundleName, String locale,
         boolean utf8, boolean createSkeletons) throws IOException
   {
      Properties targetProp = new Properties();

      if (srcDoc == null)
      {
         for (TextFlowTarget target : doc.getTextFlowTargets())
         {
            textFlowTargetToProperty(target.getResId(), target, targetProp, createSkeletons);
         }
      }
      else
      {
         Map<String, TextFlowTarget> targets = new HashMap<String, TextFlowTarget>();
         if (doc != null)
         {
            for (TextFlowTarget target : doc.getTextFlowTargets())
            {
               targets.put(target.getResId(), target);
            }
         }
         for (TextFlow textFlow : srcDoc.getTextFlows())
         {
            TextFlowTarget target = targets.get(textFlow.getId());
            textFlowTargetToProperty(textFlow.getId(), target, targetProp, createSkeletons);
         }
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

   private static void textFlowTargetToProperty(String resId, TextFlowTarget target, Properties targetProp, boolean createSkeletons)
   {
      if (target == null || target.getState() != ContentState.Approved || target.getContents() == null || target.getContents().size() == 0)
      {
         // don't save fuzzy or empty values
         if (createSkeletons)
         {
            targetProp.setProperty(resId, "");
         }
         return;
      }
      List<String> contents = target.getContents();
      if (contents.size() != 1)
      {
         throw new RuntimeException("file format does not support plural forms: resId=" + resId);
      }
      targetProp.setProperty(target.getResId(), contents.get(0));
      SimpleComment simpleComment = target.getExtensions(true).findByType(SimpleComment.class);
      if (simpleComment != null && simpleComment.getValue() != null)
      {
         targetProp.setComment(target.getResId(), simpleComment.getValue());
      }
   }

}

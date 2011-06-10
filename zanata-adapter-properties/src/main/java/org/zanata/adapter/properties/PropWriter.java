package org.zanata.adapter.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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
      PrintStream out = new PrintStream(new FileOutputStream(baseFile));
      props.store(out, null);
   }

   public static void write(final TranslationsResource doc, final File baseDir, String bundleName, String locale) throws IOException
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
      PrintStream out2 = new PrintStream(new FileOutputStream(langFile));
      targetProp.store(out2, null);
   }

}

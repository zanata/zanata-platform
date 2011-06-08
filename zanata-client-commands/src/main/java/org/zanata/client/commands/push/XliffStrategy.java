package org.zanata.client.commands.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.xml.sax.InputSource;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.common.util.PathUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public class XliffStrategy implements PushStrategy
{
   private static final String XML_EXTENSION = ".xml";

   StringSet extensions = new StringSet("comment");
   XliffReader reader = new XliffReader();
   WildcardFileFilter srcFileFilter;
   NotFileFilter targetFileFilter;
   
   private PushOptions opts;

   @Override
   public void setPushOptions(PushOptions opts)
   {
      this.opts = opts;

   }

   @Override
   public StringSet getExtensions()
   {
      return extensions;
   }

   @Override
   public Set<String> findDocNames(File srcDir) throws IOException
   {
      Set<String> localDocNames = new HashSet<String>();
      srcFileFilter = new WildcardFileFilter(opts.getSourcePattern());
      targetFileFilter = new NotFileFilter(srcFileFilter);

      Collection<File> files = FileUtils.listFiles(srcDir, srcFileFilter, TrueFileFilter.TRUE);

      for (File f : files)
      {
         String fileName = f.getPath();
         String baseName = fileName.substring(0, fileName.length() - XML_EXTENSION.length());

         String pathSeparator = "/";
         baseName = PathUtil.getRelativePath(baseName, srcDir.getPath(), pathSeparator);

         if (baseName.contains("_"))
         {
            for (LocaleMapping locMap : opts.getLocales())
            {
               String loc = locMap.getJavaLocale();
               if (baseName.contains("_" + loc))
               {
                  baseName = baseName.replace("_" + loc, "");
               }
            }
         }
         localDocNames.add(baseName);
      }
      return localDocNames;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      File srcFile = new File(sourceDir, docName + "_" + new LocaleId(opts.getSourceLang()).toJavaName() + ".xml");
      InputSource srcInputSource = new InputSource(new FileInputStream(srcFile));
      return reader.extractTemplate(srcInputSource, new LocaleId(opts.getSourceLang()), docName);
   }

   @Override
   public void visitTranslationResources(String docUri, String docName, Resource srcDoc, TranslationResourcesVisitor visitor)
   {
      for (LocaleMapping locale : opts.getLocales())
      {
         File transFile = new File(opts.getTransDir(), docName + "_" + locale.getJavaLocale() + ".xml");
         if (transFile.exists())
         {
            InputSource inputSource = new InputSource(transFile.toURI().toString());
            inputSource.setEncoding("utf8");
            TranslationsResource targetDoc = reader.extractTarget(inputSource, srcDoc);
            visitor.visit(locale, targetDoc);
         }
      }

   }
}

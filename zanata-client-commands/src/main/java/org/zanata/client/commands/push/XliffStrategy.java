package org.zanata.client.commands.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.xml.sax.InputSource;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;


public class XliffStrategy implements PushStrategy
{
   StringSet extensions = new StringSet("comment");
   XliffReader reader = new XliffReader();
   Set<String> sourceFiles;
   
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
   public Set<String> findDocNames(File srcDir, List<String> includes, List<String> excludes) throws IOException
   {
      sourceFiles = new HashSet<String>();
      Set<String> localDocNames = new HashSet<String>();

      includes.add("**/*.xml");
      for (LocaleMapping locMap : opts.getLocales())
      {
         String loc = locMap.getJavaLocale().toLowerCase();
         excludes.add("**/*_" + loc + ".xml");
      }

      DirectoryScanner dirScanner = new DirectoryScanner();
      dirScanner.setBasedir(srcDir);
      dirScanner.setCaseSensitive(false);
      dirScanner.setExcludes((String[]) excludes.toArray(new String[excludes.size()]));
      dirScanner.setIncludes((String[]) includes.toArray(new String[includes.size()]));
      dirScanner.scan();
      String[] files = dirScanner.getIncludedFiles();

      for (String relativeFilePath : files)
      {
         sourceFiles.add(relativeFilePath);
         String baseName = FilenameUtils.removeExtension(relativeFilePath);
         baseName = trimLocaleFromFile(baseName);
         localDocNames.add(baseName);
      }
      return localDocNames;
   }

   private String trimLocaleFromFile(String fileName)
   {
      if (fileName.contains("_"))
      {
         String loc = new LocaleId(opts.getSourceLang()).toJavaName();
         if (StringUtils.containsIgnoreCase(fileName, "_" + loc))
         {
            fileName = fileName.replaceAll("_" + loc, "");
         }
      }
      return fileName;
   }
   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      File srcFile = null;
	   for(String file:sourceFiles){
         if (file.startsWith(docName) && file.endsWith(".xml"))
         {
	         srcFile = new File(sourceDir, file);
	         break;
	      }
	   }
      InputSource srcInputSource = new InputSource(new FileInputStream(srcFile));
      return reader.extractTemplate(srcInputSource, new LocaleId(opts.getSourceLang()), docName);
   }

   @Override
   public void visitTranslationResources(String docName, Resource srcDoc, TranslationResourcesVisitor visitor) throws FileNotFoundException
   {
      for (LocaleMapping locale : opts.getLocales())
      {
         File transFile = new File(opts.getTransDir(), docName + "_" + locale.getJavaLocale() + ".xml");
         if (transFile.exists())
         {
            InputSource inputSource = new InputSource(new FileInputStream(transFile));
            inputSource.setEncoding("utf8");
            // TODO opts.getUseSourceOrder()
            TranslationsResource targetDoc = reader.extractTarget(inputSource);
            visitor.visit(locale, targetDoc);
         }
      }
   }
}

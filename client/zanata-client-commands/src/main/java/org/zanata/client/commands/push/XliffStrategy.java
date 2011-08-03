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
import org.xml.sax.InputSource;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;


public class XliffStrategy extends AbstractPushStrategy
{
   XliffReader reader = new XliffReader();
   Set<String> sourceFiles;
   
   public XliffStrategy()
   {
      super(new StringSet("comment"), ".xml");
   }

   @Override
   public Set<String> findDocNames(File srcDir, List<String> includes, List<String> excludes) throws IOException
   {
      sourceFiles = new HashSet<String>();
      Set<String> localDocNames = new HashSet<String>();

      String[] files = getSrcFiles(srcDir, includes, excludes, true);

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
         String loc = new LocaleId(getOpts().getSourceLang()).toJavaName();
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
         if (file.startsWith(docName) && file.endsWith(getFileExtension()))
         {
	         srcFile = new File(sourceDir, file);
	         break;
	      }
	   }
      InputSource srcInputSource = new InputSource(new FileInputStream(srcFile));
      return reader.extractTemplate(srcInputSource, new LocaleId(getOpts().getSourceLang()), docName);
   }

   @Override
   public void visitTranslationResources(String docName, Resource srcDoc, TranslationResourcesVisitor visitor) throws FileNotFoundException
   {
      for (LocaleMapping locale : getOpts().getLocales())
      {
         String filename = docNameToFilename(docName, locale);
         File transFile = new File(getOpts().getTransDir(), filename);
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

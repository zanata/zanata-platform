package org.zanata.client.commands.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
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
   WildcardFileFilter srcPatternFilter;
   TargetFileFilter targetFileFilter;
   AndFileFilter srcFileFilter;
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
   public Set<String> findDocNames(File srcDir) throws IOException
   {
      sourceFiles = new HashSet<String>();
      Set<String> localDocNames = new HashSet<String>();
      
      srcPatternFilter = new WildcardFileFilter(opts.getSourcePattern());
      targetFileFilter = new TargetFileFilter(opts.getLocales(), XML_EXTENSION);
      srcFileFilter = new AndFileFilter(srcPatternFilter, targetFileFilter);

      Collection<File> files = FileUtils.listFiles(srcDir, srcFileFilter, TrueFileFilter.TRUE);

      for (File file : files)
      {
         String baseName = file.getPath();
         baseName = PathUtil.getRelativePath(baseName, srcDir.getPath(), "/");
         sourceFiles.add(baseName);
         baseName = baseName.substring(0, baseName.length() - XML_EXTENSION.length());

         if (baseName.contains("_"))
         {
               String loc = new LocaleId(opts.getSourceLang()).toJavaName();
               if (StringUtils.containsIgnoreCase(baseName, "_" + loc))
               {
                  baseName = baseName.replaceAll("_" + loc, "");
               }
         }
         localDocNames.add(baseName);
      }
      return localDocNames;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      File srcFile = null;
	   for(String file:sourceFiles){
	      if(file.startsWith(docName) && file.endsWith(XML_EXTENSION)){
	         srcFile = new File(sourceDir, file);
	         break;
	      }
	   }
      InputSource srcInputSource = new InputSource(new FileInputStream(srcFile));
      return reader.extractTemplate(srcInputSource, new LocaleId(opts.getSourceLang()), docName);
   }

   @Override
   public void visitTranslationResources(String docUri, String docName, Resource srcDoc, TranslationResourcesVisitor visitor) throws FileNotFoundException
   {
      for (LocaleMapping locale : opts.getLocales())
      {
         File transFile = new File(opts.getTransDir(), docName + "_" + locale.getJavaLocale() + XML_EXTENSION);
         if (transFile.exists())
         {
            InputSource inputSource = new InputSource(new FileInputStream(transFile));
            inputSource.setEncoding("utf8");
            TranslationsResource targetDoc = reader.extractTarget(inputSource);
            visitor.visit(locale, targetDoc);
         }
      }
   }
}

/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.client.commands.push;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.fedorahosted.openprops.Properties;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.util.PathUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

class PropertiesStrategy implements PushStrategy
{
   private StringSet extensions = new StringSet("comment");
   private PushOptions opts;

   private String docNameToFilename(String docName)
   {
      return docName + ".properties";
   }

   private String docNameToFilename(String docName, LocaleMapping locale)
   {
      return docName + "_" + locale.getJavaLocale() + ".properties";
   }

   @Override
   public Set<String> findDocNames(File srcDir) throws IOException
   {
      Set<String> localDocNames = new HashSet<String>();
      TargetFileFilter filter = new TargetFileFilter(opts.getLocales(), ".properties");
      Collection<File> files = FileUtils.listFiles(srcDir, filter, TrueFileFilter.TRUE);
      for (File f : files)
      {
         String fileName = f.getPath();
         String baseName = removeDotProperties(fileName);
         String relativeName = PathUtil.getRelativePath(baseName, srcDir.getPath());
         localDocNames.add(relativeName);
      }
      return localDocNames;
   }

   @Override
   public StringSet getExtensions()
   {
      return extensions;
   }

   private Properties loadPropFile(File propFile) throws FileNotFoundException, IOException
   {
      InputStream is = new BufferedInputStream(new FileInputStream(propFile));
      try
      {
         Properties props = new Properties();
         props.load(is);
         return props;
      }
      finally
      {
         is.close();
      }
   }

   private Resource loadResource(String docName, File propFile) throws IOException
   {
      // TODO consider using PropReader
      Resource doc = new Resource(docName);
      // doc.setContentType(contentType);
      Properties props = loadPropFile(propFile);
      for (String key : props.keySet())
      {
         TextFlow textflow = propEntryToTextFlow(props, key);
         doc.getTextFlows().add(textflow);
      }
      return doc;
   }

   private TextFlow propEntryToTextFlow(Properties props, String key)
   {
      String content = props.getProperty(key);
      LocaleId sourceLoc = new LocaleId(opts.getSourceLang());
      TextFlow textflow = new TextFlow(key, sourceLoc, content);
      String comment = props.getComment(key);
      if (comment != null)
      {
         SimpleComment simpleComment = new SimpleComment(comment);
         textflow.getExtensions(true).add(simpleComment);
      }
      return textflow;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      String filename = docNameToFilename(docName);
      File propFile = new File(sourceDir, filename);
      return loadResource(docName, propFile);
   }

   private TranslationsResource loadTranslationsResource(Resource srcDoc, File transFile, boolean useSourceOrder) throws IOException
   {
      // TODO consider using PropReader
      TranslationsResource targetDoc = new TranslationsResource();
      Properties props = loadPropFile(transFile);
      if (opts.getUseSourceOrder())
      {
         for (TextFlow tf : srcDoc.getTextFlows())
         {
            String key = tf.getId();
            addPropEntryToDoc(targetDoc, props, key);
         }
      }
      else
      {
         for (String key : props.keySet())
         {
            addPropEntryToDoc(targetDoc, props, key);
         }
      }
      return targetDoc;
   }

   private void addPropEntryToDoc(TranslationsResource targetDoc, Properties props, String key)
   {
      String content = props.getProperty(key);
      if (content == null)
         return;
      TextFlowTarget textFlowTarget = new TextFlowTarget(key);
      textFlowTarget.setContent(content);
      textFlowTarget.setState(ContentState.Approved);
      String comment = props.getComment(key);
      if (comment != null)
      {
         SimpleComment simpleComment = new SimpleComment(comment);
         textFlowTarget.getExtensions(true).add(simpleComment);
      }
      targetDoc.getTextFlowTargets().add(textFlowTarget);
   }

   private String removeDotProperties(String propFileName)
   {
      return propFileName.substring(0, propFileName.length() - ".properties".length());
   }

   @Override
   public void setPushOptions(PushOptions opts)
   {
      this.opts = opts;
   }

   @Override
   public void visitTranslationResources(String docName, Resource srcDoc, TranslationResourcesVisitor callback) throws IOException
   {
      for (LocaleMapping locale : opts.getLocales())
      {
         String filename = docNameToFilename(docName, locale);
         File transFile = new File(opts.getTransDir(), filename);
         if (transFile.exists())
         {
            TranslationsResource targetDoc = loadTranslationsResource(srcDoc, transFile, opts.getUseSourceOrder());
            callback.visit(locale, targetDoc);
         }
         else
         {
            // no translation found in 'locale' for current doc
         }
      }
   }

}

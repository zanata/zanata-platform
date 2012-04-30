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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.zanata.adapter.properties.PropReader;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * NB: you must initialise this object with init() after setPushOptions()
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
class PropertiesStrategy extends AbstractPushStrategy
{
   // "8859_1" is used in Properties.java...
   private static final String ISO_8859_1 = "ISO-8859-1";

   private PropReader propReader;

   private final String charset;

   public PropertiesStrategy()
   {
      this(ISO_8859_1);
   }

   public PropertiesStrategy(String charset)
   {
      super(new StringSet("comment"), ".properties");
      this.charset = charset;
   }

   @Override
   public void init()
   {
      this.propReader = new PropReader(
            charset,
            new LocaleId(getOpts().getSourceLang()),
            ContentState.Approved);
   }

   @Override
   public Set<String> findDocNames(File srcDir, List<String> includes, List<String> excludes, boolean includeDefaultExclude) throws IOException
   {
      Set<String> localDocNames = new HashSet<String>();

      String[] files = getSrcFiles(srcDir, includes, excludes, true, includeDefaultExclude);

      for (String relativeFilePath : files)
      {
         String baseName = FilenameUtils.removeExtension(relativeFilePath);
         localDocNames.add(baseName);
      }
      return localDocNames;
   }

   private Resource loadResource(String docName, File propFile) throws IOException
   {
      Resource doc = new Resource(docName);
      // doc.setContentType(contentType);
      propReader.extractTemplate(doc, new FileInputStream(propFile));
      return doc;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      String filename = docNameToFilename(docName);
      File propFile = new File(sourceDir, filename);
      return loadResource(docName, propFile);
   }

   private TranslationsResource loadTranslationsResource(Resource srcDoc, File transFile) throws IOException
   {
      TranslationsResource targetDoc = new TranslationsResource();
      propReader.extractTarget(targetDoc, new FileInputStream(transFile));
      return targetDoc;
   }

   @Override
   public void visitTranslationResources(String docName, Resource srcDoc, TranslationResourcesVisitor callback) throws IOException
   {
      for (LocaleMapping locale : getOpts().getLocaleMapList())
      {
         String filename = docNameToFilename(docName, locale);
         File transFile = new File(getOpts().getTransDir(), filename);
         if (transFile.exists())
         {
            TranslationsResource targetDoc = loadTranslationsResource(srcDoc, transFile);
            callback.visit(locale, targetDoc);
         }
         else
         {
            // no translation found in 'locale' for current doc
         }
      }
   }
}

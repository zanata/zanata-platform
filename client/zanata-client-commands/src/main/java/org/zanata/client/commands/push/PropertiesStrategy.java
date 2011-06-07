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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.fedorahosted.openprops.Properties;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.TextFlowExtension;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;

class PropertiesStrategy implements PushStrategy
{
   StringSet extensions = new StringSet("comment");
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

      BasePropertiesFilter filter = new BasePropertiesFilter(opts.getLocales());
      Iterator<File> iter = FileUtils.iterateFiles(srcDir, filter, TrueFileFilter.TRUE);

      while (iter.hasNext())
      {
         File f = iter.next();
         String fileName = f.getPath();
         String baseName = fileName.substring(0, fileName.length() - ".properties".length());
         localDocNames.add(baseName);
      }
      return localDocNames;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      Resource doc = new Resource(docName);
      // doc.setContentType(contentType);
      // doc.setExtensions(extensions)
      // doc.setLang(lang);
      File propFile = new File(sourceDir, docName + ".properties");
      Properties props = new Properties();
      InputStream is = new BufferedInputStream(new FileInputStream(propFile));
      try
      {
         props.load(is);
      }
      finally
      {
         is.close();
      }
      for (String key : props.keySet())
      {
         String content = props.getProperty(key);
         TextFlow textflow = new TextFlow(key, LocaleId.EN_US, content);
         String comment = props.getComment(key);
         if (comment != null)
         {
            TextFlowExtension tfExt = new SimpleComment(comment);
            textflow.getExtensions(true).add(tfExt);
         }
         doc.getTextFlows().add(textflow);
      }
      return doc;
   }

   @Override
   public void visitTranslationResources(String docUri, String docName, Resource srcDoc, TranslationResourcesVisitor callback)
   {
      // TODO Auto-generated method stub

   }

}
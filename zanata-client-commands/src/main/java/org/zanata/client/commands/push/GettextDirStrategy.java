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
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.client.commands.StringUtil;
import org.zanata.client.commands.gettext.PublicanUtil;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

class GettextDirStrategy implements PushStrategy
{
   StringSet extensions = new StringSet("comment;gettext");
   PoReader2 poReader = new PoReader2();
   List<LocaleMapping> locales;
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
      // populate localDocNames by looking in pot directory
      String[] srcFiles = PublicanUtil.findPotFiles(srcDir);
      for (String potName : srcFiles)
      {
         String docName = StringUtil.removeFileExtension(potName, ".pot");
         localDocNames.add(docName);
      }
      return localDocNames;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName)
   {
      File srcFile = new File(sourceDir, docName + ".pot");
      InputSource srcInputSource = new InputSource(srcFile.toURI().toString());
      // load 'srcDoc' from pot/${docID}.pot
      return poReader.extractTemplate(srcInputSource, new LocaleId(opts.getSourceLang()), docName);
   }

   private List<LocaleMapping> findLocales()
   {
      if (locales != null)
         return locales;
      if (opts.getPushTrans())
      {
         if (opts.getLocales() != null)
         {
            locales = PublicanUtil.findLocales(opts.getTransDir(), opts.getLocales());
            if (locales.size() == 0)
            {
               PushCommand.log.warn("option 'pushTrans' is set, but none of the configured locale directories was found (check zanata.xml)");
            }
         }
         else
         {
            locales = PublicanUtil.findLocales(opts.getTransDir());
            if (locales.size() == 0)
            {
               PushCommand.log.warn("option 'pushTrans' is set, but no locale directories were found");
            }
            else
            {
               PushCommand.log.info("option 'pushTrans' is set, but no locales specified in configuration: importing " + locales.size() + " directories");
            }
         }
      }
      return locales;
   }

   @Override
   public void visitTranslationResources(String docUri, String docName, Resource srcDoc, TranslationResourcesVisitor callback)
   {
      for (LocaleMapping locale : findLocales())
      {
         File localeDir = new File(opts.getTransDir(), locale.getLocalLocale());
         File transFile = new File(localeDir, docName + ".po");
         if (transFile.exists())
         {
            InputSource inputSource = new InputSource(transFile.toURI().toString());
            inputSource.setEncoding("utf8");
            TranslationsResource targetDoc = poReader.extractTarget(inputSource, srcDoc);
            callback.visit(locale, targetDoc);
         }
      }
   }

}
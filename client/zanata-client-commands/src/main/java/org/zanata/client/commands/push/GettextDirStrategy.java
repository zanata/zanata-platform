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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.client.commands.gettext.PublicanUtil;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

class GettextDirStrategy extends AbstractPushStrategy
{
   private static final Logger log = LoggerFactory.getLogger(GettextDirStrategy.class);

   PoReader2 poReader = new PoReader2();
   List<LocaleMapping> locales;

   public GettextDirStrategy()
   {
      super(new StringSet("comment;gettext"), ".pot");
   }

   public Set<String> findDocNames(File srcDir, List<String> includes, List<String> excludes, boolean includeDefaultExclude) throws IOException
   {
      Set<String> localDocNames = new HashSet<String>();

      // populate localDocNames by looking in pot directory
      String[] srcFiles = getSrcFiles(srcDir, includes, excludes, false, includeDefaultExclude);

      for (String potName : srcFiles)
      {
         String docName = FilenameUtils.removeExtension(potName);
         localDocNames.add(docName);
      }
      return localDocNames;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      File srcFile = new File(sourceDir, docName + getFileExtension());
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
      try
      {
         InputSource potInputSource = new InputSource(bis);
         potInputSource.setEncoding("utf8");
         // load 'srcDoc' from pot/${docID}.pot
         return poReader.extractTemplate(potInputSource, new LocaleId(getOpts().getSourceLang()), docName);
      }
      finally
      {
         bis.close();
      }
   }

   private List<LocaleMapping> findLocales()
   {
      if (locales != null)
         return locales;
      if (getOpts().getPushTrans())
      {
         if (getOpts().getLocales() != null)
         {
            locales = PublicanUtil.findLocales(getOpts().getTransDir(), getOpts().getLocales());
            if (locales.size() == 0)
            {
               log.warn("option 'pushTrans' is set, but none of the configured locale directories was found (check zanata.xml)");
            }
         }
         else
         {
            locales = PublicanUtil.findLocales(getOpts().getTransDir());
            if (locales.size() == 0)
            {
               log.warn("option 'pushTrans' is set, but no locale directories were found");
            }
            else
            {
               log.info("option 'pushTrans' is set, but no locales specified in configuration: importing " + locales.size() + " directories");
            }
         }
      }
      return locales;
   }

   @Override
   public void visitTranslationResources(String docName, Resource srcDoc, TranslationResourcesVisitor callback) throws IOException
   {
      for (LocaleMapping locale : findLocales())
      {
         File localeDir = new File(getOpts().getTransDir(), locale.getLocalLocale());
         File transFile = new File(localeDir, docName + ".po");
         if (transFile.canRead())
         {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(transFile));
            try
            {
               InputSource inputSource = new InputSource(bis);
               inputSource.setEncoding("utf8");
               TranslationsResource targetDoc = poReader.extractTarget(inputSource, srcDoc, getOpts().getUseSrcOrder());
               callback.visit(locale, targetDoc);
            }
            finally
            {
               bis.close();
            }
         }
      }
   }
}
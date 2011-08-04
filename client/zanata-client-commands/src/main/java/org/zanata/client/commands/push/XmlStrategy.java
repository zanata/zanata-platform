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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class XmlStrategy extends AbstractPushStrategy
{
   private JAXBContext jaxbContext;
   private Unmarshaller unmarshaller;

   public XmlStrategy()
   {
      super(new StringSet("comment;gettext"), ".xml");
      try
      {
         jaxbContext = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
         unmarshaller = jaxbContext.createUnmarshaller();
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Set<String> findDocNames(File srcDir, List<String> includes, List<String> excludes) throws IOException
   {
      Set<String> localDocNames = new HashSet<String>();

      String[] files = getSrcFiles(srcDir, includes, excludes, true);

      for (String relativeFilePath : files)
      {
         String baseName = FilenameUtils.removeExtension(relativeFilePath);
         localDocNames.add(baseName);
      }
      return localDocNames;
   }

   @Override
   public Resource loadSrcDoc(File sourceDir, String docName) throws IOException
   {
      try
      {
         String filename = docNameToFilename(docName);
         File srcFile = new File(sourceDir, filename);
         Resource resource = (Resource) unmarshaller.unmarshal(srcFile);
         return resource;
      }
      catch (JAXBException e)
      {
         throw new IOException(e);
      }
   }

   @Override
   public void visitTranslationResources(String docName, Resource srcDoc, TranslationResourcesVisitor visitor) throws IOException
   {
      try
      {
         for (LocaleMapping locale : getOpts().getLocales())
         {
            String filename = docNameToFilename(docName, locale);
            File transFile = new File(getOpts().getTransDir(), filename);
            if (transFile.exists())
            {
               TranslationsResource targetDoc = (TranslationsResource) unmarshaller.unmarshal(transFile);
               visitor.visit(locale, targetDoc);
            }
            else
            {
               // no translation found in 'locale' for current doc
            }
         }
      }
      catch (JAXBException e)
      {
         throw new IOException(e);
      }
   }
}

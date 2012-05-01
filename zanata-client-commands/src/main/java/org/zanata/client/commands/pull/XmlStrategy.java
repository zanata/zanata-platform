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

package org.zanata.client.commands.pull;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.PathUtil;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class XmlStrategy implements PullStrategy
{
   private JAXBContext jaxbContext;
   private Marshaller marshaller;
   private PullOptions pullOptions;
   StringSet extensions = new StringSet("comment;gettext");

   public XmlStrategy()
   {
      try
      {
         jaxbContext = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
         marshaller = jaxbContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void setPullOptions(PullOptions opts)
   {
      this.pullOptions = opts;
   }

   @Override
   public boolean needsDocToWriteTrans()
   {
      return false;
   }

   private String docNameToFilename(String docName)
   {
      return docName + ".xml";
   }

   private String docNameToFilename(String docName, LocaleMapping locale)
   {
      return docName + "_" + locale.getJavaLocale() + ".xml";
   }

   @Override
   public void writeSrcFile(Resource doc) throws IOException
   {
      try
      {
         String filename = docNameToFilename(doc.getName());
         File srcFile = new File(pullOptions.getSrcDir(), filename);
         PathUtil.makeParents(srcFile);
         marshaller.marshal(doc, srcFile);
      }
      catch (JAXBException e)
      {
         throw new IOException(e);
      }
   }

   @Override
   public void writeTransFile(Resource doc, String docName, LocaleMapping locale, TranslationsResource targetDoc) throws IOException
   {
      try
      {
         String filename = docNameToFilename(docName, locale);
         File transFile = new File(pullOptions.getTransDir(), filename);
         PathUtil.makeParents(transFile);
         marshaller.marshal(targetDoc, transFile);
      }
      catch (JAXBException e)
      {
         throw new IOException(e);
      }
   }

   @Override
   public StringSet getExtensions()
   {
      return extensions;
   }
}

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
package org.zanata.client.commands.pushGlossary;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FilenameUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IGlossaryResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.Glossary;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class PushGlossaryCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PushGlossaryCommand.class);

   private static final Map<String, AbstractPushGlossaryReader> glossaryReaders = new HashMap<String, AbstractPushGlossaryReader>();

   private final PushGlossaryOptions opts;
   private final IGlossaryResource glossaryResource;
   private final URI uri;

   {
      glossaryReaders.put("po", new GlossaryPoReader());
      // strategies.put("cvs", new cvsStrategy());
      // strategies.put("po", new poStrategy());
   }

   public PushGlossaryCommand(PushGlossaryOptions opts, ZanataProxyFactory factory, IGlossaryResource glossaryResource, URI uri)
   {
      super(opts, factory);
      this.opts = opts;
      this.glossaryResource = glossaryResource;
      this.uri = uri;
   }

   private PushGlossaryCommand(PushGlossaryOptions opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getGlossaryResource(), factory.getGlossaryResourceURI());
   }

   public PushGlossaryCommand(PushGlossaryOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   private AbstractPushGlossaryReader getReader(String fileName)
   {
      String fileExtension = FilenameUtils.getExtension(fileName);
      AbstractPushGlossaryReader reader = glossaryReaders.get(fileExtension);
      if (reader == null)
      {
         throw new RuntimeException("unknown file type: " + fileExtension);
      }
      reader.setOpts(opts);
      return reader;
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", opts.getUrl());
      log.info("Username: {}", opts.getUsername());
      log.info("Source language: {}", opts.getSourceLang());
      log.info("Translation language: {}", opts.getTransLang());
      log.info("All translation comment: {}", opts.getAllTransComments());
      log.info("Glossary file: {}", opts.getGlossaryFile());

      File glossaryFile = opts.getGlossaryFile();

      if (!glossaryFile.exists())
      {
         throw new RuntimeException("File '" + glossaryFile + "' does not exist - check glossaryFile option");
      }

      AbstractPushGlossaryReader reader = getReader((opts.getGlossaryFile().getName()));

      JAXBContext jc = null;
      Marshaller m = null;

      if (opts.isDebugSet())
      {
         jc = JAXBContext.newInstance(Glossary.class);
      }
      if (opts.isDebugSet())
      {
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }

      // deleteTargetLocaleGlossaryFromServer(opts.getTransLang());

      log.info("pushing glossary document [{}] to server", glossaryFile.getName());
      
      Glossary glossary = reader.extractGlossary(glossaryFile);
      log.debug(glossary.toString());
      ClientResponse<Glossary> response = glossaryResource.put(glossary);
      ClientUtility.checkResult(response, uri);
   }

   // private void deleteTargetLocaleGlossaryFromServer(String transLang)
   // {
   // log.info("deleting glossaries with locale [{}] from server", transLang);
   // ClientResponse<String> response = glossaryResource.deleteGlossary(new
   // LocaleId(transLang));
   // ClientUtility.checkResult(response, uri);
   // }

}


 
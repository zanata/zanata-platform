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

package org.zanata.client.commands;

import java.io.Console;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public abstract class PushPullCommand<O extends PushPullOptions> extends ConfigurableProjectCommand<O>
{
   private static final Logger log = LoggerFactory.getLogger(PushPullCommand.class);

   protected final ITranslationResources translationResources;
   protected URI uri;
   private Marshaller marshaller;

   public PushPullCommand(O opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PushPullCommand(O opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PushPullCommand(O opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   protected void confirmWithUser(String message) throws IOException
   {
      if (getOpts().isInteractiveMode())
      {
         Console console = System.console();
         if (console == null)
            throw new RuntimeException("console not available: please run Maven from a console, or use batch mode (mvn -B)");
         console.printf(message + "\nAre you sure (y/n)? ");
         expectYes(console);
      }
   }

   protected static void expectYes(Console console) throws IOException
   {
      String line = console.readLine();
      if (line == null)
         throw new IOException("console stream closed");
      if (!line.toLowerCase().equals("y") && !line.toLowerCase().equals("yes"))
         throw new RuntimeException("operation aborted by user");
   }

   protected void debug(Object jaxbElement)
   {
      try
      {
         if (getOpts().isDebugSet())
         {
            StringWriter writer = new StringWriter();
            getMarshaller().marshal(jaxbElement, writer);
            log.debug("{}", writer);
         }
      }
      catch (JAXBException e)
      {
         log.debug(e.toString(), e);
      }
   }

   /**
    * @return
    * @throws JAXBException 
    */
   private Marshaller getMarshaller() throws JAXBException
   {
      if (marshaller == null)
      {
         JAXBContext jc = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
         marshaller = jc.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }
      return marshaller;
   }

}

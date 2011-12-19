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
package org.zanata.client.commands.glossary.delete;

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IGlossaryResource;
import org.zanata.rest.client.ZanataProxyFactory;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class GlossaryDeleteCommand extends ConfigurableCommand<GlossaryDeleteOptions>
{
   private static final Logger log = LoggerFactory.getLogger(GlossaryDeleteCommand.class);

   private final IGlossaryResource glossaryResource;
   private final URI uri;

   public GlossaryDeleteCommand(GlossaryDeleteOptions opts, ZanataProxyFactory factory, IGlossaryResource glossaryResource, URI uri)
   {
      super(opts, factory);
      this.glossaryResource = glossaryResource;
      this.uri = uri;
   }

   private GlossaryDeleteCommand(GlossaryDeleteOptions opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getGlossaryResource(), factory.getGlossaryResourceURI());
   }

   public GlossaryDeleteCommand(GlossaryDeleteOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", getOpts().getUrl());
      log.info("Username: {}", getOpts().getUsername());
      log.info("Locale to delete: {}", getOpts().getlang());
      log.info("Delete entire glossary?: {}", getOpts().getAllGlossary());

      ClientResponse<String> response;

      if (getOpts().getAllGlossary())
      {
         response = glossaryResource.deleteGlossaries();
      }
      else if (!StringUtils.isEmpty(getOpts().getlang()))
      {
         response = glossaryResource.deleteGlossary(new LocaleId(getOpts().getlang()));
      }
      else
      {
         throw new RuntimeException("Option 'zanata.lang' is required.");
      }
      ClientUtility.checkResult(response, uri);
   }
}

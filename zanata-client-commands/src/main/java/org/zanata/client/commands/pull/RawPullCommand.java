/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.push.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IFileResource;
import org.zanata.rest.service.FileResource;

/**
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class RawPullCommand extends PushPullCommand<CommonPullOptions>
{
   private static final Logger log = LoggerFactory.getLogger(RawPullCommand.class);

   private IFileResource fileResource;

   public RawPullCommand(CommonPullOptions opts)
   {
      super(opts);
      this.fileResource = getRequestFactory().getFileResource();
   }

   @Override
   public void run() throws Exception
   {
      LocaleList locales = getOpts().getLocaleMapList();
      if (locales == null)
         throw new ConfigException("no locales specified");
      RawPullStrategy strat = new RawPullStrategy();
      strat.setPullOptions(getOpts());

      List<String> docNamesForModule = getQualifiedDocNamesForCurrentModuleFromServer();

      // TODO compare docNamesForModule with localDocNames, offer to delete obsolete translations from filesystem
      if (docNamesForModule.isEmpty())
      {
         log.info("No documents in remote module: {}; nothing to do", getOpts().getCurrentModule());
         return;
      }
      log.info("Pulling {} docs for this module from the server", docNamesForModule.size());
      log.debug("Doc names: {}", docNamesForModule);

      PushPullType pullType = getOpts().getPullType();
      boolean pullSrc = pullType == PushPullType.Both || pullType == PushPullType.Source;
      boolean pullTarget = pullType == PushPullType.Both || pullType == PushPullType.Trans;

      if (pullSrc)
      {
         log.warn("Pull Type set to '" + pullType + "': existing source-language files may be overwritten/deleted");
         confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
      }
      else
      {
         confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
      }

      for (String qualifiedDocName : docNamesForModule)
      {
         // TODO add filtering by file type? e.g. pull all dtd documents only.

         String localDocName = unqualifiedDocName(qualifiedDocName);

         if (pullSrc)
         {
            ClientResponse response = fileResource.downloadSourceFile(getOpts().getProj(),
                  getOpts().getProjectVersion(), FileResource.FILETYPE_RAW_SOURCE_DOCUMENT, qualifiedDocName);
            if (response.getResponseStatus() == Status.NOT_FOUND)
            {
               log.warn("No raw source document is available for [{}]. Skipping.", qualifiedDocName);
            }
            else
            {
               ClientUtility.checkResult(response, uri);
               InputStream srcDoc = (InputStream) response.getEntity(InputStream.class);
               if (srcDoc != null)
               {
                  strat.writeSrcFile(localDocName, srcDoc);
               }
            }
         }

         if(pullTarget)
         {
            for (LocaleMapping locMapping : locales)
            {
               LocaleId locale = new LocaleId(locMapping.getLocale());
               ClientResponse response = fileResource.downloadTranslationFile(getOpts().getProj(),
                     getOpts().getProjectVersion(), locale.getId(),
                     FileResource.FILETYPE_TRANSLATED_APPROVED, qualifiedDocName);
               if (response.getResponseStatus() == Response.Status.NOT_FOUND)
               {
                     log.info("No raw translation document found in locale {} for document [{}]", locale, qualifiedDocName);
               }
               else
               {
                  ClientUtility.checkResult(response, uri);
                  InputStream transDoc = (InputStream) response.getEntity(InputStream.class);
                  if (transDoc != null)
                  {
                     strat.writeTransFile(localDocName, locMapping, transDoc);
                  }
               }
            }
         }
      }
   }

}

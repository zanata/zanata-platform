package org.zanata.client.commands;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.adapter.po.PoWriter2;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 * @deprecated
 * @see org.zanata.client.commands.pull.PullCommand
 */
public class PublicanPullCommand extends ConfigurableProjectCommand<PublicanPullOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PublicanPullCommand.class);

   private final ITranslationResources translationResources;
   private final URI uri;

   public PublicanPullCommand(PublicanPullOptions opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PublicanPullCommand(PublicanPullOptions opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PublicanPullCommand(PublicanPullOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   @Override
   protected String getProjectType()
   {
      return PROJECT_TYPE_PUBLICAN;
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", getOpts().getUrl());
      log.info("Project: {}", getOpts().getProj());
      log.info("Version: {}", getOpts().getProjectVersion());
      log.info("Username: {}", getOpts().getUsername());
      if (getOpts().getExportPot())
      {
         log.info("Exporting source and target (translation) documents");
         log.info("POT directory (originals): {}", getOpts().getDstDirPot());
      }
      else
      {
         log.info("Exporting target documents (translations) only");
      }
      log.info("PO base directory (translations): {}", getOpts().getDstDir());

      LocaleList locales = getOpts().getLocales();
      if (locales == null)
         throw new ConfigException("no locales specified");
      PoWriter2 poWriter = new PoWriter2();
      StringSet extensions = new StringSet("gettext;comment");

      ClientResponse<List<ResourceMeta>> listResponse = translationResources.get(null);
      ClientUtility.checkResult(listResponse, uri);
      List<ResourceMeta> resourceMetaList = listResponse.getEntity();
      for (ResourceMeta resourceMeta : resourceMetaList)
      {
         String docName = resourceMeta.getName();
         // TODO follow a Link
         String docUri = RestUtil.convertToDocumentURIId(docName);
         ClientResponse<Resource> resourceResponse = translationResources.getResource(docUri, extensions);
         ClientUtility.checkResult(resourceResponse, uri);
         Resource doc = resourceResponse.getEntity();
         if (getOpts().getExportPot())
         {
            log.info("writing POT for document {}", docName);
            poWriter.writePotToDir(getOpts().getDstDirPot(), doc);
         }

         for (LocaleMapping locMapping : locales)
         {
            LocaleId locale = new LocaleId(locMapping.getLocale());

            ClientResponse<TranslationsResource> transResponse = translationResources.getTranslations(docUri, locale, extensions);
            // ignore 404 (no translation yet for specified document)
            if (transResponse.getResponseStatus() == Response.Status.NOT_FOUND)
            {
               log.info("no translations found in locale {} for document {}", locale, docName);
               continue;
            }
            ClientUtility.checkResult(transResponse, uri);
            TranslationsResource targetDoc = transResponse.getEntity();

            String localeDir = locMapping.getLocalLocale();
            log.info("writing PO translations in locale {} for document {}", locale, docName);
            poWriter.writePo(getOpts().getDstDir(), doc, localeDir, targetDoc);
         }
      }

   }

}

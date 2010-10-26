package net.openl10n.flies.client.commands;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;

import net.openl10n.flies.adapter.po.PoWriter2;
import net.openl10n.flies.client.config.LocaleList;
import net.openl10n.flies.client.config.LocaleMapping;
import net.openl10n.flies.client.exceptions.ConfigException;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.RestUtil;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class PublicanPullCommand extends ConfigurableProjectCommand
{
   private static final Logger log = LoggerFactory.getLogger(PublicanPullCommand.class);

   private final PublicanPullOptions opts;
   private final ITranslationResources translationResources;
   private final URI uri;

   public PublicanPullCommand(PublicanPullOptions opts, FliesClientRequestFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.opts = opts;
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PublicanPullCommand(PublicanPullOptions opts, FliesClientRequestFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PublicanPullCommand(PublicanPullOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }


   @Override
   public void run() throws Exception
   {
      // TODO needs DocSet support
      log.debug("Flies server: {}", opts.getUrl());
      log.debug("Project: {}", opts.getProj());
      log.debug("Version: {}", opts.getProjectVersion());
      log.info("writing POT/PO files to {}", opts.getDstDir());

      LocaleList locales = opts.getLocales();
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
         if (opts.getExportPot())
         {
            log.info("writing POT for document {}", docName);
            poWriter.writePot(opts.getDstDir(), doc);
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
            poWriter.writePo(opts.getDstDir(), doc, localeDir, targetDoc);
         }
      }

   }

}

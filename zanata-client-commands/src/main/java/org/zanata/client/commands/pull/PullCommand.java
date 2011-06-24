package org.zanata.client.commands.pull;

import java.io.Console;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableProjectCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
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
 */
public class PullCommand extends ConfigurableProjectCommand
{
   private static final Logger log = LoggerFactory.getLogger(PullCommand.class);

   private static final Map<String, PullStrategy> strategies = new HashMap<String, PullStrategy>();

   {
      strategies.put("properties", new PropertiesStrategy());
      strategies.put("podir", new GettextDirStrategy());
      strategies.put("xliff", new XliffStrategy());
   }

   Marshaller m = null;

   private final PullOptions opts;
   private final ITranslationResources translationResources;
   private final URI uri;

   public PullCommand(PullOptions opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.opts = opts;
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PullCommand(PullOptions opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PullCommand(PullOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   private PullStrategy getStrategy(String strategyType)
   {
      PullStrategy strat = strategies.get(strategyType);
      if (strat == null)
      {
         throw new RuntimeException("unknown project type: " + opts.getProjectType());
      }
      strat.setPullOptions(opts);
      return strat;
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", opts.getUrl());
      log.info("Project: {}", opts.getProj());
      log.info("Version: {}", opts.getProjectVersion());
      log.info("Username: {}", opts.getUsername());
      if (opts.getPullSrc())
      {
         log.info("Pulling source and target (translation) documents");
         log.info("Source-language directory (originals): {}", opts.getSrcDir());
      }
      else
      {
         log.info("Pulling target documents (translations) only");
      }
      log.info("Target-language base directory (translations): {}", opts.getTransDir());

      if (opts.getPullSrc())
      {
         log.warn("pullSrc option is set: existing source-language files may be overwritten/deleted");
         confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
      }
      else
      {
         confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
      }
      PullStrategy strat = getStrategy(opts.getProjectType());

      JAXBContext jc = null;
      if (opts.isDebugSet()) // || opts.getValidate())
      {
         jc = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
      }
      if (opts.isDebugSet())
      {
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }

      LocaleList locales = opts.getLocales();
      if (locales == null)
         throw new ConfigException("no locales specified");

      ClientResponse<List<ResourceMeta>> listResponse = translationResources.get(null);
      ClientUtility.checkResult(listResponse, uri);
      List<ResourceMeta> resourceMetaList = listResponse.getEntity();
      for (ResourceMeta resourceMeta : resourceMetaList)
      {
         Resource doc = null;
         String docName = resourceMeta.getName();
         // TODO follow a Link
         String docUri = RestUtil.convertToDocumentURIId(docName);
         if (strat.needsDocToWriteTrans() || opts.getPullSrc())
         {
            ClientResponse<Resource> resourceResponse = translationResources.getResource(docUri, strat.getExtensions());
            ClientUtility.checkResult(resourceResponse, uri);
            doc = resourceResponse.getEntity();
         }
         if (opts.getPullSrc())
         {
            log.info("writing source file for document {}", docName);
            strat.writeSrcFile(doc);
         }

         for (LocaleMapping locMapping : locales)
         {
            LocaleId locale = new LocaleId(locMapping.getLocale());

            ClientResponse<TranslationsResource> transResponse = translationResources.getTranslations(docUri, locale, strat.getExtensions());
            // ignore 404 (no translation yet for specified document)
            if (transResponse.getResponseStatus() == Response.Status.NOT_FOUND)
            {
               log.info("no translations found in locale {} for document {}", locale, docName);
               continue;
            }
            ClientUtility.checkResult(transResponse, uri);
            TranslationsResource targetDoc = transResponse.getEntity();

            log.info("writing translation file in locale {} for document {}", locMapping.getLocalLocale(), docName);
            strat.writeTransFile(doc, locMapping, targetDoc);
         }
      }

   }

   private void confirmWithUser(String message) throws IOException
   {
      if (opts.isInteractiveMode())
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
         if (opts.isDebugSet())
         {
            StringWriter writer = new StringWriter();
            m.marshal(jaxbElement, writer);
            log.debug("{}", writer);
         }
      }
      catch (JAXBException e)
      {
         log.debug(e.toString(), e);
      }
   }

}

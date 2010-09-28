package net.openl10n.flies.rest.service;


import java.net.URI;

import javax.ws.rs.core.MediaType;

import net.openl10n.flies.FliesDBUnitSeamTest;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "seam-tests" })
public abstract class ResourceTranslationServiceSeamTest extends FliesDBUnitSeamTest
{
   private static final String AUTH_KEY = "b6d7044e9ee3b2447c28fb7c50d86d98";
   private static final String USERNAME = "admin";
   private static final String DOCUMENTS_DATA_DBUNIT_XML = "net/openl10n/flies/test/model/DocumentsData.dbunit.xml";
   private static final String LOCALE_DATA_DBUNIT_XML = "META-INF/testdata/LocalesData.dbunit.xml";
   private static final String PROJECTS_DATA_DBUNIT_XML = "net/openl10n/flies/test/model/ProjectsData.dbunit.xml";

   protected ITranslationResources translationResource;
   private String projectSlug = "sample-project";
   private String iter = "1.1";

   private class MetaTypeAccept implements ClientExecutionInterceptor
   {
      @SuppressWarnings("rawtypes")
      @Override
      public ClientResponse execute(ClientExecutionContext ctx) throws Exception
      {
         // ctx.getRequest().getHeaders().add("Accept",
         // MediaType.APPLICATION_XML);
         // ctx.getRequest().getHeaders().add("Accept",
         // MediaType.APPLICATION_JSON);
         ctx.getRequest().getHeaders().add("Content-Type", MediaType.APPLICATION_XML);
         return ctx.proceed();
      }

   }

   @BeforeMethod
   public void setup() throws Exception
   {
      FliesClientRequestFactory clientRequestFactory = new FliesClientRequestFactory(new URI("http://example.com/"), USERNAME, AUTH_KEY, new SeamMockClientExecutor(this));
      clientRequestFactory.registerPrefixInterceptor(new MetaTypeAccept());
      translationResource = clientRequestFactory.getTranslationResources(projectSlug, iter);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(PROJECTS_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(LOCALE_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      afterTestOperations.add(new DataSetOperation(PROJECTS_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(LOCALE_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
   }
}

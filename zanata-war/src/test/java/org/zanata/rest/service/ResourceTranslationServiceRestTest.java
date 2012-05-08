package org.zanata.rest.service;


import javax.ws.rs.core.MediaType;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDBUnitSeamTest;
import org.zanata.ZanataRestTest;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.TestProxyFactory;

public abstract class ResourceTranslationServiceRestTest extends ZanataRestTest
{
   private static final String DOCUMENTS_DATA_DBUNIT_XML = "org/zanata/test/model/DocumentsData.dbunit.xml";
   private static final String LOCALE_DATA_DBUNIT_XML = "org/zanata/test/model/LocalesData.dbunit.xml";
   private static final String PROJECTS_DATA_DBUNIT_XML = "org/zanata/test/model/ProjectsData.dbunit.xml";
   private static final String ACCOUNT_DATA_DBUNIT_XML = "org/zanata/test/model/AccountData.dbunit.xml";

   protected ISourceDocResource sourceDocResource;
   protected ITranslatedDocResource translationResource;
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

   @BeforeMethod(dependsOnMethods = "prepareRestEasyFramework")
   public void setup() throws Exception
   {
      sourceDocResource = getClientRequestFactory().createProxy(ISourceDocResource.class, createBaseURI("/projects/p/" + projectSlug + "/iterations/i/" + iter + "/r"));
      translationResource = getClientRequestFactory().createProxy(ITranslatedDocResource.class, createBaseURI("/projects/p/" + projectSlug + "/iterations/i/" + iter + "/r"));
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(PROJECTS_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(LOCALE_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));

      afterTestOperations.add(new DataSetOperation(PROJECTS_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(LOCALE_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
   }
}

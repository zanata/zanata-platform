package org.zanata.rest.service;


import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDBUnitSeamTest;
import org.zanata.rest.client.IGlossaryResource;
import org.zanata.rest.dto.VersionInfo;

@Test(groups = { "seam-tests" })
public abstract class GlossaryServiceSeamTest extends ZanataDBUnitSeamTest
{
   private static final String AUTH_KEY = "b6d7044e9ee3b2447c28fb7c50d86d98";
   private static final String USERNAME = "admin";
   private static final String GLOSSARY_DATA_DBUNIT_XML = "org/zanata/test/model/GlossaryData.dbunit.xml";

   protected IGlossaryResource glossaryResource;

   private class MetaTypeAccept implements ClientExecutionInterceptor
   {
      @SuppressWarnings("rawtypes")
      @Override
      public ClientResponse execute(ClientExecutionContext ctx) throws Exception
      {
         ctx.getRequest().getHeaders().add("Content-Type", MediaType.APPLICATION_XML);
         return ctx.proceed();
      }

   }

   @BeforeMethod
   public void setup() throws Exception
   {
      TestProxyFactory clientRequestFactory = new TestProxyFactory(new URI("http://example.com/"), USERNAME, AUTH_KEY, new SeamMockClientExecutor(this), new VersionInfo("SNAPSHOT", ""));
      clientRequestFactory.registerPrefixInterceptor(new MetaTypeAccept());
      glossaryResource = clientRequestFactory.getGlossaryResource();
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation(GLOSSARY_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      afterTestOperations.add(new DataSetOperation(GLOSSARY_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
   }
}

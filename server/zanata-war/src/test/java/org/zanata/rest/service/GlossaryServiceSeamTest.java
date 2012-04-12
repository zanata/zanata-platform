package org.zanata.rest.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.ws.rs.core.MediaType;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDBUnitSeamTest;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.IGlossaryResource;
import org.zanata.rest.client.TestProxyFactory;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

@Test(groups = { "seam-tests" })
public class GlossaryServiceSeamTest extends ZanataDBUnitSeamTest
{
   private static final String GLOSSARY_DATA_DBUNIT_XML = "org/zanata/test/model/GlossaryData.dbunit.xml";
   private static final String ACCOUNT_DATA_DBUNIT_XML = "org/zanata/test/model/AccountData.dbunit.xml";
   private static final String LOCALE_DATA_DBUNIT_XML = "org/zanata/test/model/LocalesData.dbunit.xml";

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
      TestProxyFactory clientRequestFactory = new TestProxyFactory(new SeamMockClientExecutor(this));
      clientRequestFactory.registerPrefixInterceptor(new MetaTypeAccept());

      glossaryResource = clientRequestFactory.getGlossaryResource();

   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation(GLOSSARY_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(LOCALE_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      afterTestOperations.add(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(GLOSSARY_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
      afterTestOperations.add(new DataSetOperation(LOCALE_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
   }

   @Test
   public void testPutGlossaries()
   {
      Glossary glossary = new Glossary();
      GlossaryEntry glossaryEntry1 = new GlossaryEntry();
      glossaryEntry1.setSrcLang(LocaleId.EN_US);
      glossaryEntry1.setSourcereference("TEST SOURCE REF DATA");

      GlossaryTerm glossaryTerm1 = new GlossaryTerm();
      glossaryTerm1.setLocale(LocaleId.EN_US);
      glossaryTerm1.setContent("test data content 1 (source lang)");
      glossaryTerm1.getComments().add("COMMENT 1");

      glossaryEntry1.getGlossaryTerms().add(glossaryTerm1);

      glossary.getGlossaryEntries().add(glossaryEntry1);

      ClientResponse<String> response = glossaryResource.put(glossary);
      
      ClientResponse<Glossary> response1 = glossaryResource.getEntries();
      assertThat(response1.getEntity().getGlossaryEntries().size(), is(1));

   }
}

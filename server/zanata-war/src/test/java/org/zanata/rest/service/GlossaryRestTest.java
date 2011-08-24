package org.zanata.rest.service;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Identity;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.GlossaryDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.rest.client.IGlossaryResource;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.service.impl.LocaleServiceImpl;

public class GlossaryRestTest extends ZanataRestTest
{

   private final String RESOURCE_PATH = "/glossary";
   IMocksControl mockControl = EasyMock.createControl();
   Identity mockIdentity = mockControl.createMock(Identity.class);
   IGlossaryResource glossaryService;

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod
   void reset()
   {
      mockControl.reset();
   }

   @BeforeMethod(dependsOnMethods = "prepareRestEasyFramework")
   public void createClient()
   {
      this.glossaryService = getClientRequestFactory().createProxy(IGlossaryResource.class, createBaseURI(RESOURCE_PATH));
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/GlossaryData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Override
   protected void prepareResources()
   {
      GlossaryDAO glossaryDAO = new GlossaryDAO(getSession());
      AccountDAO accountDAO = new AccountDAO(getSession());

      LocaleServiceImpl localeService = new LocaleServiceImpl();
      LocaleDAO localeDAO = new LocaleDAO(getSession());
      localeService.setLocaleDAO(localeDAO);

      GlossaryService glossaryService = new GlossaryService(glossaryDAO, accountDAO, mockIdentity, localeService);

      resources.add(glossaryService);
   }

   @Test
   public void retrieveAllGlossaryTerm()
   {
      ClientResponse<Glossary> response = glossaryService.getEntries();
      assertThat(response.getStatus(), is(200));

      List<GlossaryEntry> glossaryEntries = response.getEntity().getGlossaryEntries();
      assertThat(glossaryEntries.size(), is(1));

      List<GlossaryTerm> glossaryTerms = glossaryEntries.get(0).getGlossaryTerms();
      assertThat(glossaryTerms.size(), is(3));
   }

   @Test
   public void retrieveGlossaryTermWithLocale()
   {
      ClientResponse<Glossary> response = glossaryService.get(LocaleId.EN_US);
      assertThat(response.getStatus(), is(200));

      List<GlossaryEntry> glossaryEntries = response.getEntity().getGlossaryEntries();
      assertThat(glossaryEntries.size(), is(1));

      List<GlossaryTerm> glossaryTerms = glossaryEntries.get(0).getGlossaryTerms();
      assertThat(glossaryTerms.size(), is(1));

      Assert.assertNotNull(glossaryEntries.get(0).getSrcLang());
      assertThat(glossaryEntries.get(0).getSrcLang(), is(LocaleId.EN_US));

   }

   @Test
   public void retrieveNonExistingGlossaryTermLocale()
   {
      ClientResponse<Glossary> response = glossaryService.get(LocaleId.FR);
      List<GlossaryEntry> glossaryEntries = response.getEntity().getGlossaryEntries();
      assertThat(glossaryEntries.size(), is(0));
   }

   @Test
   public void putGlossary()
   {
      mockIdentity.checkPermission(anyObject(HGlossaryEntry.class), eq("insert"));
      mockIdentity.checkPermission(anyObject(HGlossaryEntry.class), eq("insert"));

      Glossary glossary = new Glossary();
      GlossaryEntry glossaryEntry1 = new GlossaryEntry();
      glossaryEntry1.setSrcLang(LocaleId.EN_US);
      glossaryEntry1.setSourcereference("TEST SOURCE REF DATA");
      
      GlossaryTerm glossaryTerm1 = new GlossaryTerm();
      glossaryTerm1.setLocale(LocaleId.EN_US);
      glossaryTerm1.setContent("TEST DATA 1 EN_US");
      glossaryTerm1.getComments().add("COMMENT 1");

      GlossaryTerm glossaryTerm2 = new GlossaryTerm();
      glossaryTerm2.setLocale(LocaleId.DE);
      glossaryTerm2.setContent("TEST DATA 2 DE");
      glossaryTerm2.getComments().add("COMMENT 2");

      glossaryEntry1.getGlossaryTerms().add(glossaryTerm1);
      glossaryEntry1.getGlossaryTerms().add(glossaryTerm2);

      GlossaryEntry glossaryEntry2 = new GlossaryEntry();
      glossaryEntry2.setSrcLang(LocaleId.EN_US);
      glossaryEntry2.setSourcereference("TEST SOURCE REF DATA2");

      GlossaryTerm glossaryTerm3 = new GlossaryTerm();
      glossaryTerm3.setLocale(LocaleId.EN_US);
      glossaryTerm3.setContent("TEST DATA 3 EN_US");
      glossaryTerm3.getComments().add("COMMENT 3");

      GlossaryTerm glossaryTerm4 = new GlossaryTerm();
      glossaryTerm4.setLocale(LocaleId.DE);
      glossaryTerm4.setContent("TEST DATA 4 DE");
      glossaryTerm4.getComments().add("COMMENT 4");

      glossaryEntry2.getGlossaryTerms().add(glossaryTerm3);
      glossaryEntry2.getGlossaryTerms().add(glossaryTerm4);

      glossary.getGlossaryEntries().add(glossaryEntry1);
      glossary.getGlossaryEntries().add(glossaryEntry2);

      ClientResponse<Glossary> response = glossaryService.put(glossary);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
   }

   @Test
   public void deleteAllGlossaries()
   {
      ClientResponse<String> response = glossaryService.deleteGlossaries();
      assertThat(response.getStatus(), is(200));

      ClientResponse<Glossary> response1 = glossaryService.getEntries();

      List<GlossaryEntry> glossaryEntries = response1.getEntity().getGlossaryEntries();
      assertThat(glossaryEntries.size(), is(0));
   }

   @Test
   public void deleteGlossaryTermWithLocale()
   {
      ClientResponse<String> response = glossaryService.deleteGlossary(LocaleId.ES);
      assertThat(response.getStatus(), is(200));

      ClientResponse<Glossary> response1 = glossaryService.getEntries();
      List<GlossaryEntry> glossaryEntries = response1.getEntity().getGlossaryEntries();

      assertThat(glossaryEntries.get(0).getGlossaryTerms().size(), is(2));
   }

}
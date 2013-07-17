package org.zanata.rest.service;

import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.jboss.seam.security.Identity;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowStreamDAO;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;

public class TMXDummyRestTest extends ZanataRestTest
{

   @Mock
   private ZanataIdentity mockIdentity;
   private TranslationMemoryResource tmService;
   private SeamAutowire seam = SeamAutowire.instance();

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod(dependsOnMethods = "prepareRestEasyFramework")
   public void createClient()
   {
      MockitoAnnotations.initMocks(this);
      this.tmService = getClientRequestFactory().createProxy(TranslationMemoryResource.class, createBaseURI("/rest/tm"));
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      // empty (not using DBUnit)
   }

//   @Override
//   protected Map<String, String> createPropertiesMap()
//   {
//      return ImmutableMap.of(
//            "hibernate.connection.driver_class", "com.mysql.jdbc.Driver",
//            "hibernate.connection.url", "jdbc:mysql://localhost/refimpl_db?characterEncoding=UTF-8",
//            "hibernate.connection.username", "root",
//            "hibernate.connection.password", "",
//            "hibernate.dialect", "org.zanata.util.ZanataMySQL5InnoDBDialect");
//   }

   @Override
   protected void prepareResources()
   {
      seam.reset();
      Session session = getSession();
      // @formatter:off
      seam.ignoreNonResolvable()
            .use("session", session)
            .use("identity", mockIdentity);
      // @formatter:on

      TranslationMemoryResourceService tmService = seam.autowire(TranslationMemoryResourceService.class);
      resources.add(seam.autowire(TextFlowStreamDAO.class));
      resources.add(tmService);
   }

   @Test(enabled = false)
   void testGetTmx()
   {
      Response response = tmService.getProjectIterationTranslationMemory("iok", "6.4", new LocaleId("as"));
      System.out.println(response.getEntity());
   }

}
package org.zanata.rest.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.CoreMatchers;
import org.hibernate.Session;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Identity;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.IGlossaryResource;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.GlossaryFileServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

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
      this.tmService = getClientRequestFactory().createProxy(TranslationMemoryResource.class, createBaseURI("/tm"));
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      // empty (not using DBUnit)
   }

   @Override
   protected Map<String, String> createPropertiesMap()
   {
      return ImmutableMap.of(
            "hibernate.connection.driver_class", "com.mysql.jdbc.Driver",
            "hibernate.connection.url", "jdbc:mysql://localhost/refimpl_db?characterEncoding=UTF-8",
            "hibernate.connection.username", "root",
            "hibernate.connection.password", "",
            "hibernate.dialect", "org.zanata.util.ZanataMySQL5InnoDBDialect");
   }

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

      TranslationMemoryService tmService = seam.autowire(TranslationMemoryService.class);

      resources.add(tmService);
   }

   @Test
   void testGetTmx()
   {
      tmService.getProjectIterationTranslationMemory("iok", "6.4", new LocaleId("as"));
   }

}
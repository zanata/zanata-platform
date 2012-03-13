package org.zanata.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDBUnitSeamTest;
import org.zanata.rest.client.IVersionResource;
import org.zanata.rest.client.TestProxyFactory;
import org.zanata.rest.dto.VersionInfo;


public class VersionSeamTest extends ZanataDBUnitSeamTest
{
   private IVersionResource version;
   private final Logger log = LoggerFactory.getLogger(VersionSeamTest.class);

   private static final String ACCOUNT_DATA_DBUNIT_XML = "org/zanata/test/model/AccountData.dbunit.xml";

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML, DatabaseOperation.CLEAN_INSERT));
      afterTestOperations.add(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML, DatabaseOperation.DELETE_ALL));
   }

   @BeforeMethod
   public void setup() throws Exception
   {
      log.debug("setup test version service");
      TestProxyFactory clientRequestFactory = new TestProxyFactory(new SeamMockClientExecutor(this));
      version = clientRequestFactory.createIVersionResource();
   }

   @Test
   public void test()
   {
      log.debug("test version service");
      VersionInfo v = version.get().getEntity();
      String ver = v.getVersionNo();
      log.debug("expected versoin:" + ver);
      assertThat(v.getVersionNo(), is(ver));
   }

}

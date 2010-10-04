package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.URI;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.openl10n.flies.FliesDBUnitSeamTest;
import net.openl10n.flies.rest.client.IVersion;
import net.openl10n.flies.rest.dto.resource.VersionInfo;

public class VersionSeamTest extends FliesDBUnitSeamTest
{
   private static final String AUTH_KEY = "b6d7044e9ee3b2447c28fb7c50d86d98";
   private static final String USERNAME = "admin";
   private IVersion version;
   private final Logger log = LoggerFactory.getLogger(VersionSeamTest.class);

   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @BeforeMethod
   public void setup() throws Exception
   {
      log.debug("setup test version service");
      FliesTestClientRequestFactory clientRequestFactory = new FliesTestClientRequestFactory(new URI("http://example.com/"), USERNAME, AUTH_KEY, new SeamMockClientExecutor(this), new VersionInfo("SNAPSHOT", ""));
      version = clientRequestFactory.getVersionInfo();
   }

   @Test
   public void test()
   {
      log.debug("test version service");
      VersionInfo v = version.get();
      String ver = v.getVersionNo();
      log.debug("expected versoin:" + ver);
      assertThat(v.getVersionNo(), is(ver));
   }

}

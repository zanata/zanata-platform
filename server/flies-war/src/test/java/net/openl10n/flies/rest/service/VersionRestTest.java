package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import net.openl10n.flies.FliesRestTest;
import net.openl10n.flies.rest.client.IVersion;
import net.openl10n.flies.rest.dto.VersionInfo;

public class VersionRestTest extends FliesRestTest
{
   private final String RESOURCE_PATH = "/version";
   String vVar = "1.0SNAPSHOT";
   String vBuild = "20101009";
   VersionInfo ver = new VersionInfo(vVar, vBuild);
   private final Logger log = LoggerFactory.getLogger(VersionRestTest.class);

   @Override
   protected void prepareResources()
   {
      VersionService service = new VersionService(ver);
      resources.add(service);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @Test
   public void retrieveVersionInfo()
   {
      IVersion resource;
      log.debug("setup test version service");
      resource = getClientRequestFactory().createProxy(IVersion.class, createBaseURI(RESOURCE_PATH));

      VersionInfo entity = resource.get();
      assertThat(entity.getVersionNo(), is(vVar));
      assertThat(entity.getBuildTimeStamp(), is(vBuild));
   }


}

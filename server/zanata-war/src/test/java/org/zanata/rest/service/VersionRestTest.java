package org.zanata.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.service.VersionResource;
import org.zanata.rest.service.VersionService;


public class VersionRestTest extends ZanataRestTest
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
      VersionResource resource;
      log.debug("setup test version service");
      resource = getClientRequestFactory().createProxy(VersionResource.class, createBaseURI(RESOURCE_PATH));

      VersionInfo entity = resource.get();
      assertThat(entity.getVersionNo(), is(vVar));
      assertThat(entity.getBuildTimeStamp(), is(vBuild));
   }


}

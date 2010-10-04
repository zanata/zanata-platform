package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


import net.openl10n.flies.FliesInit;
import net.openl10n.flies.FliesRestTest;
import net.openl10n.flies.rest.client.IVersion;
import net.openl10n.flies.rest.dto.resource.VersionInfo;

public class VersionRestTest extends FliesRestTest
{
   private final String RESOURCE_PATH = "/version";
   IMocksControl mockControl = EasyMock.createControl();
   FliesInit mock = mockControl.createMock(FliesInit.class);
   private final Logger log = LoggerFactory.getLogger(VersionRestTest.class);

   @Override
   protected void prepareResources()
   {
      VersionService service = new VersionService(mock);
      resources.add(service);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @Test
   public void retrieveVersionInfo()
   {
      String vVar = "1.0SNAPSHOT";
      String vBuild = "20101009";
      EasyMock.expect(mock.getVersion()).andReturn(vVar).anyTimes();
      EasyMock.expect(mock.getBuildTimestamp()).andReturn(vBuild).anyTimes();
      mockControl.replay();
      IVersion resource;
      log.debug("setup test version service");
      resource = getClientRequestFactory().createProxy(IVersion.class, createBaseURI(RESOURCE_PATH));

      VersionInfo entity = resource.get();
      assertThat(entity.getVersionNo(), is(vVar));
      assertThat(entity.getBuildTimeStamp(), is(vBuild));
      mockControl.verify();
   }


}

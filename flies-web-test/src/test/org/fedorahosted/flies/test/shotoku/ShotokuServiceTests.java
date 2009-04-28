package org.fedorahosted.flies.test.shotoku;

import org.fedorahosted.flies.shotoku.ShotokuUpdateService;
import org.jboss.seam.contexts.ApplicationContext;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class ShotokuServiceTests extends SeamTest{

	@Test
	public void testStartStopService(){
		ShotokuUpdateService updateService = (ShotokuUpdateService) getInstance("shotokuUpdateService");
		assert updateService != null;
	}
}

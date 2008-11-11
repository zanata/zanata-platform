package org.fedorahosted.flies.test;

import org.testng.annotations.Test;
import org.jboss.seam.mock.SeamTest;

public class DamnedLiesImportTest extends SeamTest {

	@Test
	public void test_damnedLiesImport() throws Exception {
		new FacesRequest() {
			@Override
			protected void updateModelValues() throws Exception {				
				//set form input to model attributes
				setValue("#{DamnedLiesImport.value}", "seam");
			}
			@Override
			protected void invokeApplication() {
				//call action methods here
				invokeMethod("#{DamnedLiesImport.damnedLiesImport}");
			}
			@Override
			protected void renderResponse() {
				//check model attributes if needed
				assert getValue("#{DamnedLiesImport.value}").equals("seam");
			}
		}.run();
	}
}

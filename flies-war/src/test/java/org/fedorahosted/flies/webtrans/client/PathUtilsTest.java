package org.fedorahosted.flies.webtrans.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PathUtilsTest {

	private static final String[][] pathPatterns = new String[][] { 
		new String[] { "/Mydoc.html", "/" },
		new String[] { "/flies/path/org.flies/App.html", "/flies/path/" },
	}; 
	
	@DataProvider(name = "pathPatterns")
	public String[][] createPathPatterns() {
		return pathPatterns;
	}

	@Test(dataProvider = "pathPatterns")
	public void getContextRootWithVariousValidPatterns(String data, String expected) {
		assertThat("Converting " +data , PathUtils.getContextRoot(data) , is(expected));
	}
	
}

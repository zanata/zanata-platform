package org.fedorahosted.flies.client.ant.properties;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("nls")
public class LocalTest extends AbstractBuildTest {
    /**
     * This helps Infinitest, since it doesn't know about the taskdefs inside
     * build.xml
     */
    @SuppressWarnings("unchecked")
    static Class[] testedClasses = { 
	Props2DocsTask.class, Docs2PropsTask.class };

    public LocalTest(String name) {
	super(name);
    }
    
    @Override
    protected String getBuildFile() {
        return "src/test/resources/org/fedorahosted/flies/client/ant/properties/build.xml";
    }

    public static Test suite() {
	TestSuite suite = new TestSuite(LocalTest.class.getName());
	
	suite.addTest(new LocalTest("props2docs"));
	suite.addTest(new LocalTest("docs2props"));
	suite.addTest(new LocalTest("roundtriplocal"));
	return suite;
    }

//    @Override
//    protected void tearDown() throws Exception {
//	String outDir = getProject().getProperty("out.dir");
//	if (outDir != null) {
//	    TestUtil.delete(new File(outDir));
//	}
//	super.tearDown();
//    }

}

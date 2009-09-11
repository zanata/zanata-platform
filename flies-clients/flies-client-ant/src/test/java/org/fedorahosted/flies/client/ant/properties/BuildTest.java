package org.fedorahosted.flies.client.ant.properties;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.tools.ant.BuildFileTest;

@SuppressWarnings("nls")
public class BuildTest extends BuildFileTest {
    /**
     * This helps Infinitest, since it doesn't know about the taskdefs inside
     * build.xml
     */
    @SuppressWarnings("unchecked")
    static Class[] testedClasses = { 
	Props2DocsTask.class, Docs2PropsTask.class };

    public BuildTest(String name) {
	super(name);
    }

    @Override
    protected void runTest() throws Throwable {
	try {
	    System.out.println("Executing build target '"+getName()+"'");
	    executeTarget(getName());
	} finally {
	    System.out.print(getLog());
	    System.out.print(getOutput());
	    System.err.print(getError());
	}
    }
    
    @Override
    protected void setUp() throws Exception {
	// work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
	System.getProperties().remove("basedir");
	configureProject("src/test/resources/org/fedorahosted/flies/client/ant/properties/build.xml");
    }
    
    public static Test suite() {
	TestSuite suite = new TestSuite(BuildTest.class.getName());
	suite.addTest(new BuildTest("props2docs"));
	suite.addTest(new BuildTest("docs2props"));
	suite.addTest(new BuildTest("roundtriplocal"));
//	suite.addTest(new BuildTest("roundtripremote"));
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

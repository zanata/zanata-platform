package org.fedorahosted.flies.client.ant.properties;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("nls")
public class RemoteTest extends AbstractBuildTest {
    /**
     * This helps Infinitest, since it doesn't know about the taskdefs inside
     * build.xml
     */
    @SuppressWarnings("unchecked")
    static Class[] testedClasses = { 
	Props2DocsTask.class, Docs2PropsTask.class };

    public RemoteTest(String name) {
	super(name);
    }
    
    @Override
    protected String getBuildFile() {
        return "src/test/resources/org/fedorahosted/flies/client/ant/properties/build.xml";
    }

    public static Test suite() {
	TestSuite suite = new TestSuite(RemoteTest.class.getName());
	suite.addTest(new RemoteTest("roundtripremote"));
	return suite;
    }

}

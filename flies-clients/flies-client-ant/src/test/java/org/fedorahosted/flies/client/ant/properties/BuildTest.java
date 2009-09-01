package org.fedorahosted.flies.client.ant.properties;

import org.apache.tools.ant.BuildFileTest;
import org.fedorahosted.flies.client.ant.properties.Props2ProjectTask;

@SuppressWarnings("nls")
public class BuildTest extends BuildFileTest {
    /**
     * This helps Infinitest, since it doesn't know about the taskdefs inside
     * build.xml
     */
    @SuppressWarnings("unchecked")
    static Class[] testedClasses = { Props2ProjectTask.class };

    public BuildTest(String name) {
	super(name);
    }

    @Override
    protected void setUp() throws Exception {
	// work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
	System.getProperties().remove("basedir");
	configureProject("src/test/resources/org/fedorahosted/flies/client/ant/properties/build.xml");
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void test1() throws Exception {
	executeTarget(getName());
	System.out.print(getLog());
	System.out.print(getOutput());
	System.err.print(getError());
    }

    public void test2() throws Exception {
	executeTarget(getName());
	System.out.print(getLog());
	System.out.print(getOutput());
	System.err.print(getError());
    }

    public void test3() throws Exception {
	executeTarget(getName());
	System.out.print(getLog());
	System.out.print(getOutput());
	System.err.print(getError());
    }
}

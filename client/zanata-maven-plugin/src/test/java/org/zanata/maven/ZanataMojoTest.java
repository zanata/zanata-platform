package org.zanata.maven;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.zanata.client.commands.ZanataCommand;

public abstract class ZanataMojoTest<M extends Mojo, C extends ZanataCommand>
        extends AbstractMojoTestCase {
    protected IMocksControl control = EasyMock.createControl();

    protected abstract M getMojo();

    protected abstract C getMockCommand();

    @Override
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
        control.reset();
        EasyMock.expect(getMockCommand().getName()).andReturn("mockCommand")
                .anyTimes();
        EasyMock.expect(getMockCommand().isDeprecated()).andReturn(false)
                .anyTimes();
    }

    @Override
    protected void tearDown() throws Exception {
        // required
        super.tearDown();
    }

    protected void applyPomParams(String pomFile) throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader()
                .getResource("push-test/" + pomFile);
        File testPom = new File(resource.toURI());
        // This will work with "mvn test", but not with Eclipse's JUnit runner:
        // PushSimpleMojo mojo = (PushSimpleMojo) lookupMojo("push", testPom);
        // assertNotNull(mojo);
        getMockCommand().runWithActions();
        EasyMock.expectLastCall();
        control.replay();
        configureMojo(getMojo(), "zanata-maven-plugin", testPom);
        getMojo().execute();
        control.verify();
    }
}

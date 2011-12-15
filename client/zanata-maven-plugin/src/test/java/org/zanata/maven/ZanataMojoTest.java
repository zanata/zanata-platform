package org.zanata.maven;

import java.io.File;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.zanata.client.commands.ZanataCommand;

public abstract class ZanataMojoTest<M extends Mojo, C extends ZanataCommand> extends AbstractMojoTestCase
{
   protected IMocksControl control = EasyMock.createControl();

   protected abstract M getMojo();

   protected abstract C getMockCommand();

   @Override
   protected void setUp() throws Exception
   {
      // required for mojo lookups to work
      super.setUp();
      control.reset();
   }

   @Override
   protected void tearDown() throws Exception
   {
      // required
      super.tearDown();
   }

   protected void applyPomParams(String pomFile) throws Exception
   {
      File testPom = getTestFile("src/test/resources/push-test/" + pomFile);
      // This will work with "mvn test", but not with Eclipse's JUnit runner:
      // PushMojo mojo = (PushMojo) lookupMojo("push", testPom);
      // assertNotNull(mojo);
      getMockCommand().run();
      EasyMock.expectLastCall();
      control.replay();
      configureMojo(getMojo(), "zanata-maven-plugin", testPom);
      getMojo().execute();
      control.verify();
   }
}

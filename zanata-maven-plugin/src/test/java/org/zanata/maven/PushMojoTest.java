package org.zanata.maven;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class PushMojoTest extends AbstractMojoTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      // required for mojo lookups to work
      super.setUp();
   }

   @Override
   protected void tearDown() throws Exception
   {
      // required
      super.tearDown();
   }

   public void testLookup() throws Exception
   {
      File testPom = getTestFile("src/test/resources/push-test/pom.xml");
      // This will work with "mvn test", but not with Eclipse's JUnit runner:
      // PushMojo mojo = (PushMojo) lookupMojo("push", testPom);
      // assertNotNull(mojo);
      PushMojo mojo = new PushMojo();
      configureMojo(mojo, "zanata-maven-plugin", testPom);
      // mojo.execute();
   }
}

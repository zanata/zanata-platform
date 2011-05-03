package org.zanata.client.ant.po;

import org.apache.tools.ant.BuildFileTest;

public abstract class AbstractBuildTest extends BuildFileTest
{

   public AbstractBuildTest(String name)
   {
      super(name);
   }

   @Override
   protected void runTest() throws Throwable
   {
      try
      {
         System.out.println("Executing build target '" + getName() + "'");
         executeTarget(getName());
      }
      finally
      {
         System.out.print(getLog());
         System.out.print(getOutput());
         System.err.print(getError());
      }
   }

   @Override
   protected void setUp() throws Exception
   {
      // work around maven bug: http://jira.codehaus.org/browse/SUREFIRE-184
      System.getProperties().remove("basedir");
      configureProject(getBuildFile());
   }

   abstract protected String getBuildFile();

}

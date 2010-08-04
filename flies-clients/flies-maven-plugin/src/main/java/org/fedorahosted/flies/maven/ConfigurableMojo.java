package org.fedorahosted.flies.maven;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.fedorahosted.flies.client.command.ConfigurableCommand;

/**
 * Base class for Flies mojos which support configuration by the user's
 * flies.ini
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableMojo<C extends ConfigurableCommand> extends AbstractMojo
{

   private final C command;

   // @formatter:off
   /*
    * Note: The following fields are only here to hold Maven's @parameter 
    * markup, since all the setter methods actually delegate to the 
    * FliesCommand.  @parameter should work on setter methods - see
    * http://www.sonatype.com/books/mvnref-book/reference/writing-plugins-sect-param-annot.html
    * - but it doesn't.
    */
   // @formatter:on  

   /**
    * Client configuration file for Flies.
    * 
    * @parameter expression="${flies.client.config}"
    */
   @SuppressWarnings("unused")
   private File userConfig;

   /**
    * Base URL for the Flies server. Defaults to the value in flies.xml (if
    * present), or else to flies.ini.
    * 
    * @parameter expression="${flies.url}"
    */
   @SuppressWarnings("unused")
   private URL url;

   /**
    * Username for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    * 
    * @parameter expression="${flies.username}"
    */
   @SuppressWarnings("unused")
   private String username;

   /**
    * API key for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    * 
    * @parameter expression="${flies.key}"
    */
   @SuppressWarnings("unused")
   private String key;

   /**
    * Whether to enable debug mode. Defaults to the value in flies.ini.
    * 
    * @parameter expression="${flies.debug}"
    */
   @SuppressWarnings("unused")
   private boolean debug;

   /**
    * Whether to display full information about errors (ie exception stack
    * traces). Defaults to the value in flies.ini.
    * 
    * @parameter expression="${flies.errors}"
    */
   @SuppressWarnings("unused")
   private boolean errors;

   public ConfigurableMojo(C command)
   {
      this.command = command;
   }

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      try
      {
         command.initConfig();
         // TODO remove this
         getLog().info(getClass().getSimpleName());
         getCommand().run();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new MojoExecutionException("error loading Flies user config", e);
      }
   }

   public C getCommand()
   {
      return command;
   }

   public void setDebug(boolean debug)
   {
      command.setDebug(debug);
   }

   public void setErrors(boolean errors)
   {
      command.setErrors(errors);
   }

   public void setKey(String key)
   {
      command.setKey(key);
   }

   public void setUrl(URL url)
   {
      command.setUrl(url);
   }

   public void setUserConfig(File userConfig)
   {
      command.setUserConfig(userConfig);
   }

   public void setUsername(String username)
   {
      command.setUsername(username);
   }

}

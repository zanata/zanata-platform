package org.fedorahosted.flies.maven;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.fedorahosted.flies.client.commands.ConfigurableCommand;

import com.pyx4j.log4j.MavenLogAppender;

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
    * @parameter expression="${flies.userConfig}"
    *            default-value="${user.home}/.config/flies.ini"
    */
   /*
    * NB the annotation 'default-value' overrides the default in
    * ConfigurableCommand (even though the values are virtually identical)
    * because Mojos aren't meant to use System properties directly (since they
    * may be sharing a VM and its System properties)
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

   public ConfigurableMojo(C command)
   {
      this.command = command;
   }

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException
   {
      // @formatter:off
      /*
       * Configure the MavenLogAppender to use this Mojo's Maven logger. NB
       * maven-plugin-log4j.jar includes a log4j.xml to activate the
       * MavenLogAppender. See 
       * http://pyx4j.com/snapshot/pyx4j/pyx4j-maven-plugins/maven-plugin-log4j/index.html
       * In case it needs to be overridden, it looks like this:

<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE log4j:configuration PUBLIC "-//log4j//DTD//EN" "http://logging.apache.org/log4j/docs/api/org/apache/log4j/xml/log4j.dtd">
<log4j:configuration>

    <appender name="MavenLogAppender" class="com.pyx4j.log4j.MavenLogAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%m" />
        </layout>
    </appender>

    <root>
        <level value="debug" />
        <appender-ref ref="MavenLogAppender" />
    </root>

</log4j:configuration>

       */
      // @formatter:on
      
      MavenLogAppender.startPluginLog(this);
      try
      {
         getCommand().initConfig();
         getCommand().run();
      }
      catch (Exception e)
      {
         throw new MojoExecutionException("Flies mojo exception", e);
      }
      finally
      {
         MavenLogAppender.endPluginLog(this);
      }
   }

   public C getCommand()
   {
      return command;
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

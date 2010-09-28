package net.openl10n.flies.maven;

import java.io.File;
import java.net.URL;

import net.openl10n.flies.client.commands.ConfigurableOptions;
import net.openl10n.flies.client.commands.FliesCommand;
import net.openl10n.flies.client.commands.OptionsUtil;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.pyx4j.log4j.MavenLogAppender;

/**
 * Base class for Flies mojos which support configuration by the user's
 * flies.ini
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
public abstract class ConfigurableMojo extends AbstractMojo implements ConfigurableOptions
{

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
   private File userConfig;

   /**
    * Base URL for the Flies server. Defaults to the value in flies.xml (if
    * present), or else to flies.ini.
    * 
    * @parameter expression="${flies.url}"
    */
   private URL url;

   /**
    * Username for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    * 
    * @parameter expression="${flies.username}"
    */
   private String username;

   /**
    * API key for accessing the Flies REST API. Defaults to the value in
    * flies.ini.
    * 
    * @parameter expression="${flies.key}"
    */
   private String key;

   public ConfigurableMojo()
   {
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
         OptionsUtil.applyConfigFiles(this);
         FliesCommand command = initCommand();
         command.run();
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

   public abstract FliesCommand initCommand();

   // These options don't apply to Mojos (since they duplicate Maven's built-in
   // mechanisms)

   @Override
   public boolean getDebug()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setDebug(boolean debug)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean getErrors()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setErrors(boolean errors)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean getHelp()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setHelp(boolean help)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean getQuiet()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setQuiet(boolean quiet)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isDebugSet()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isErrorsSet()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isQuietSet()
   {
      throw new UnsupportedOperationException();
   }

   // these options only apply to the command line:
   @Override
   public String getCommandDescription()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public String getCommandName()
   {
      throw new UnsupportedOperationException();
   }


   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public void setKey(String key)
   {
      this.key = key;
   }

   @Override
   public URL getUrl()
   {
      return url;
   }

   @Override
   public void setUrl(URL url)
   {
      this.url = url;
   }

   @Override
   public void setUserConfig(File userConfig)
   {
      this.userConfig = userConfig;
   }

   @Override
   public String getUsername()
   {
      return username;
   }

   @Override
   public void setUsername(String username)
   {
      this.username = username;
   }

   @Override
   public File getUserConfig()
   {
      return userConfig;
   }

}

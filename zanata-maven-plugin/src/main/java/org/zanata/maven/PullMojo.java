package org.zanata.maven;

import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.push.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;

/**
 * Pulls translated text from Zanata.
 * 
 * @goal pull
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PullMojo extends PushPullMojo<PullOptions> implements PullOptions
{

   /**
    * Export source-language text from Zanata to local files, overwriting or
    * erasing existing files (DANGER!). This option is deprecated, replaced by pullType.
    * 
    * @parameter expression="${zanata.pullSrc}"
    */
   @Deprecated
   // Using string instead of boolean to know when pullSrc has been explicitly used.
   private String pullSrc;

   /**
    * Whether to create skeleton entries for strings/files which have not been translated yet
    * @parameter expression="${zanata.createSkeletons}"
    */
   private boolean createSkeletons;

   /**
    * Type of pull to perform from the server: "source" pulls source documents only.
    * "trans" pulls translation documents only.
    * "both" pulls both source and translation documents.
    *
    * @parameter expression="${zanata.pullType}" default-value="trans"
    */
   private String pullType;

   /**
    * Locales to pull from the server.
    * By default all locales in zanata.xml will be pulled.
    * Usage: -Dzanata.locales=locale1,locale2,locale3
    *
    * @parameter expression="${zanata.locales}"
    */
   private String[] locales;

   // Cached copy of the effective locales to avoid calculating it more than once
   private LocaleList effectiveLocales;


   public PullMojo() throws Exception
   {
      super();
   }

   public PullCommand initCommand()
   {
      return new PullCommand(this);
   }

   @Override
   public boolean getCreateSkeletons()
   {
      return createSkeletons;
   }

   @Override
   public PushPullType getPullType()
   {
      // if the deprecated 'pushTrans' option has been used
      if( pullSrc != null )
      {
         return Boolean.parseBoolean(pullSrc) ? PushPullType.Both : PushPullType.Trans;
      }
      else
      {
         return PushPullType.fromString(pullType);
      }
   }

   /**
    * Override the default {@link org.zanata.maven.ConfigurableProjectMojo#getLocaleMapList()} method as the push
    * command can have locales specified via command line.
    *
    * @return The locale map list taking into account the global locales in zanata.xml as well as the command line
    * argument ones.
    */
   @Override
   public LocaleList getLocaleMapList()
   {
      if( effectiveLocales == null )
      {
         if(locales != null && locales.length > 0)
         {
            // filter the locales that are specified in both the global config and the parameter list
            effectiveLocales = new LocaleList();

            for( String locale : locales )
            {
               boolean foundLocale = false;
               for(LocaleMapping lm : super.getLocaleMapList())
               {
                  if( lm.getLocale().equals(locale) ||
                        (lm.getMapFrom() != null && lm.getMapFrom().equals( locale )) )
                  {
                     effectiveLocales.add(lm);
                     foundLocale = true;
                     break;
                  }
               }

               if(!foundLocale)
               {
                  throw new ConfigException("Specified locale '" + locale + "' was not found in zanata.xml!" );
               }
            }
         }
         else
         {
            effectiveLocales = super.getLocaleMapList();
         }
      }

      return effectiveLocales;
   }

   @Override
   public String getCommandName()
   {
      return "pull";
   }
   
}

package org.zanata.util;

import java.util.EnumSet;

import org.junit.rules.ExternalResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Annotate with @TestRule or @ClassRule
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ResetDatabaseRule extends ExternalResource
{
   private final EnumSet<Config> configSet;

   public ResetDatabaseRule(Config ... configs)
   {
      configSet = EnumSet.of(Config.Empty, configs);
   }

   @Override
   protected void before() throws Throwable
   {
      if (!isResetEnabled())
      {
         return;
      }

      DatabaseHelper.database().resetFileData();

      if (configSet.contains(Config.WithData))
      {
         DatabaseHelper.database().resetDatabaseWithData();
      }
      else
      {
         log.info("reset database before");
         DatabaseHelper.database().resetData();
         DatabaseHelper.database().addAdminUser();
         DatabaseHelper.database().addTranslatorUser();
      }

   }

   private boolean isResetEnabled()
   {
      return !Boolean.valueOf(PropertiesHolder.getProperty("disable.database.reset"));
   }

   @Override
   protected void after()
   {
      // by default it will reset database after
      if (isResetEnabled() && !configSet.contains(Config.NoResetAfter))
      {
         DatabaseHelper.database().resetData();
         log.info("reset database after");
      }
   }

   public static enum Config
   {
      Empty, NoResetAfter, WithData;
   }
}

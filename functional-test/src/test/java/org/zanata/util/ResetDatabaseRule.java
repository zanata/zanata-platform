package org.zanata.util;

import java.util.EnumSet;

import org.junit.rules.ExternalResource;

/**
 * Annotate with @TestRule or @ClassRule
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
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
      DatabaseHelper.database().resetData();
      DatabaseHelper.database().addAdminUser();
      DatabaseHelper.database().addTranslatorUser();
   }

   @Override
   protected void after()
   {
      // by default it will reset database after
      if (!configSet.contains(Config.NoResetAfter))
      {
         DatabaseHelper.database().resetData();
      }
   }

   public static enum Config
   {
      Empty, NoResetAfter
   }
}

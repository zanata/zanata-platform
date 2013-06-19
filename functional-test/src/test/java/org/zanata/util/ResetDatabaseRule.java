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
      if (configSet.contains(Config.WithAdmin))
      {
         DatabaseHelper.database().addAdminUser();
      }
      if (configSet.contains(Config.WithTranslator))
      {
         DatabaseHelper.database().addTranslatorUser();
      }
   }

   public static enum Config
   {
      Empty, WithAdmin, WithTranslator
   }
}

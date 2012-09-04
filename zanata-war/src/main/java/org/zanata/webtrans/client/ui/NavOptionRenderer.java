package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.EnumMessages;
import org.zanata.webtrans.shared.rpc.NavOption;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class NavOptionRenderer extends EnumRenderer<NavOption>
{
   private final EnumMessages messages;

   @Inject
   public NavOptionRenderer(EnumMessages messages)
   {
      this.messages = messages;
   }

   @Override
   public String render(NavOption option)
   {
      switch (option)
      {
         case FUZZY:
            return messages.nextFuzzy();
         case UNTRANSLATED:
            return messages.nextUntranslated();
         case FUZZY_UNTRANSLATED:
            return messages.nextFuzzyOrUntranslated();
         default:
            return getEmptyValue();
      }
   }
}

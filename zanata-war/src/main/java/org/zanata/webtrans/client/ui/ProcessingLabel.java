package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ProcessingLabel extends Label
{
   private boolean show = false;
   private final Animation animation;

   @Inject
   public ProcessingLabel(final UiMessages messages, Resources resources)
   {
      super(messages.processing());
      animation = new Animation() {
         private double count = 0;

         @Override
         protected void onComplete()
         {
            if (show)
            {
               setText(messages.processing());
               count = 0;
               this.run(5000); //if still showing, continue to run
            }
            else
            {
               super.onComplete();
            }
         }

         @Override
         protected void onUpdate(double progress)
         {
            if (progress - count > 0.2)
            {
               setText(getText() + ".");
               count = progress;
            }
         }
      };
   }

   public void start()
   {
      show = true;
      animation.run(5000);
   }

   public void stop()
   {
      show = false;
      animation.cancel();
   }
}

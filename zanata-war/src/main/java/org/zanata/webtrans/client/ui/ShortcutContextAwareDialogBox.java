package org.zanata.webtrans.client.ui;

import java.util.Collections;
import java.util.Set;

import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ShortcutContextAwareDialogBox extends DialogBox
{
   private final KeyShortcutPresenter keyShortcutPresenter;
   private Set<ShortcutContext> activeContext = Collections.emptySet();

   public ShortcutContextAwareDialogBox(KeyShortcutPresenter keyShortcutPresenter)
   {
      super();
      this.keyShortcutPresenter = keyShortcutPresenter;
   }

   public ShortcutContextAwareDialogBox(boolean autoHide, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(autoHide);
      this.keyShortcutPresenter = keyShortcutPresenter;
   }

   public ShortcutContextAwareDialogBox(Caption captionWidget, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(captionWidget);
      this.keyShortcutPresenter = keyShortcutPresenter;
   }

   public ShortcutContextAwareDialogBox(boolean autoHide, boolean modal, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(autoHide, modal);
      this.keyShortcutPresenter = keyShortcutPresenter;
   }

   public ShortcutContextAwareDialogBox(boolean autoHide, boolean modal, Caption captionWidget, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(autoHide, modal, captionWidget);
      this.keyShortcutPresenter = keyShortcutPresenter;
   }

   @Override
   public void hide()
   {
      super.hide();
      keyShortcutPresenter.setContextActive(ShortcutContext.Popup, false);
      for (ShortcutContext shortcutContext : activeContext)
      {
         keyShortcutPresenter.setContextActive(shortcutContext, true);
      }
   }

   @Override
   public void show()
   {
      super.show();
      activeContext = keyShortcutPresenter.getActiveContexts();
      for (ShortcutContext shortcutContext : activeContext)
      {
         keyShortcutPresenter.setContextActive(shortcutContext, false);
      }
      keyShortcutPresenter.setContextActive(ShortcutContext.Popup, true);
   }
}

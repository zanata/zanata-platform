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
   private final ShortcutContext shortcutContext;
   private final KeyShortcutPresenter keyShortcutPresenter;
   private Set<ShortcutContext> activeContext = Collections.emptySet();

   public ShortcutContextAwareDialogBox(boolean autoHide, boolean modal, ShortcutContext shortcutContext, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(autoHide, modal);
      this.shortcutContext = shortcutContext;
      this.keyShortcutPresenter = keyShortcutPresenter;
   }

   @Override
   public void hide()
   {
      super.hide();
      for (ShortcutContext shortcutContext : activeContext)
      {
         keyShortcutPresenter.setContextActive(shortcutContext, true);
      }
      keyShortcutPresenter.setContextActive(shortcutContext, false);
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
      keyShortcutPresenter.setContextActive(shortcutContext, true);
   }
}

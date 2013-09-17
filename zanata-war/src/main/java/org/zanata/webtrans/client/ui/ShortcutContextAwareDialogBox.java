package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ShortcutContextAwareDialogBox extends DialogBox
{
   private final ShortcutContext modalContext;
   private final KeyShortcutPresenter keyShortcutPresenter;

   public ShortcutContextAwareDialogBox(boolean autoHide, boolean modal, ShortcutContext modalContext, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(autoHide, modal);
      this.modalContext = modalContext;
      this.keyShortcutPresenter = keyShortcutPresenter;
      // intercept esc key so that Firefox won't close event service connection
      KeyShortcut hideSelfShortcut = KeyShortcut.Builder.builder()
            .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ESCAPE))
            .setContext(modalContext)
            .setKeyEvent(KeyShortcut.KeyEvent.KEY_DOWN)
            .setPreventDefault(true)
            .setStopPropagation(true)
            .setHandler(new KeyShortcutEventHandler()
            {
               @Override
               public void onKeyShortcut(KeyShortcutEvent event)
               {
                  hide();
               }
            }).build();
      keyShortcutPresenter.register(hideSelfShortcut);
   }

   @Override
   public void hide()
   {
      super.hide();
      keyShortcutPresenter.deactivateModalContext();

   }

   @Override
   public void show()
   {
      super.show();
      keyShortcutPresenter.activateModalContext(modalContext);
   }
}

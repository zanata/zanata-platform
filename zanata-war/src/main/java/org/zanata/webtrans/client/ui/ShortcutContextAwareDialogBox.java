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
   private final ShortcutContext modalContext;
   private final KeyShortcutPresenter keyShortcutPresenter;

   public ShortcutContextAwareDialogBox(boolean autoHide, boolean modal, ShortcutContext modalContext, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(autoHide, modal);
      this.modalContext = modalContext;
      this.keyShortcutPresenter = keyShortcutPresenter;
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

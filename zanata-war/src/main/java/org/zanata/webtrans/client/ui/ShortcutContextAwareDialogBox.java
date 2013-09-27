package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.events.*;
import org.zanata.webtrans.client.keys.*;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.DialogBox;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ShortcutContextAwareDialogBox extends DialogBox {
    private final ShortcutContext modalContext;
    private final KeyShortcutPresenter keyShortcutPresenter;

    public ShortcutContextAwareDialogBox(final boolean autoHide,
            final boolean modal, ShortcutContext modalContext,
            KeyShortcutPresenter keyShortcutPresenter) {
        super(autoHide, modal);
        this.modalContext = modalContext;
        this.keyShortcutPresenter = keyShortcutPresenter;
        // intercept esc key so that Firefox won't close event service
        // connection
        KeyShortcutEventHandler handler =
                createKeyShortcutEventHandler(autoHide, modal);
        KeyShortcut hideSelfShortcut =
                KeyShortcut.Builder
                        .builder()
                        .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ESCAPE))
                        .setContext(modalContext)
                        .setKeyEvent(KeyShortcut.KeyEvent.KEY_DOWN)
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(handler).build();
        keyShortcutPresenter.register(hideSelfShortcut);
    }

    private KeyShortcutEventHandler createKeyShortcutEventHandler(
            boolean autoHide, boolean modal) {
        KeyShortcutEventHandler handler;
        if (autoHide && modal) {
            handler = new KeyShortcutEventHandler() {
                @Override
                public void onKeyShortcut(KeyShortcutEvent event) {
                    // no op
                }
            };

        } else {
            handler = new KeyShortcutEventHandler() {
                @Override
                public void onKeyShortcut(KeyShortcutEvent event) {
                    hide();
                }
            };
        }
        return handler;
    }

    @Override
    public void hide() {
        super.hide();
        keyShortcutPresenter.deactivateModalContext();
    }

    @Override
    public void center() {
        super.center();
        keyShortcutPresenter.activateModalContext(modalContext);
    }

    @Override
    public void show() {
        super.show();
        keyShortcutPresenter.activateModalContext(modalContext);
    }
}

package org.zanata.webtrans.client.presenter;

import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.NextState;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.PrevState;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.TableEditorMessages;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class EditorKeyShortcuts implements UserConfigChangeHandler {
    private final KeyShortcutPresenter keyShortcutPresenter;
    private final EventBus eventBus;
    private final UserConfigHolder configHolder;
    private final TableEditorMessages messages;

    private KeyShortcut enterSavesApprovedShortcut;
    private KeyShortcut nextStateShortcut;
    private KeyShortcut prevStateShortcut;

    private HandlerRegistration enterSavesApprovedHandlerRegistration;
    private UserConfigHolder.ConfigurationState configuration;

    @Inject
    public EditorKeyShortcuts(KeyShortcutPresenter keyShortcutPresenter,
            EventBus eventBus, UserConfigHolder configHolder,
            TableEditorMessages messages) {
        this.keyShortcutPresenter = keyShortcutPresenter;
        this.eventBus = eventBus;
        this.configHolder = configHolder;
        this.messages = messages;

        configuration = configHolder.getState();
        eventBus.addHandler(UserConfigChangeEvent.TYPE, this);
    }

    public void registerKeys(TargetContentsPresenter targetContentsPresenter) {
        registerCopyTMKeys();
        registerNavigationKeys(targetContentsPresenter);
        registerEditorActionKeys(targetContentsPresenter);
    }

    protected void registerCopyTMKeys() {
        KeyShortcut copyTM1Shortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_1))
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_1))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.copyFromTM(1))
                        .setHandler(new CopyTMKeyShortcutHandler(0)).build();

        KeyShortcut copyTM2Shortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_2))
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_2))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.copyFromTM(2))
                        .setHandler(new CopyTMKeyShortcutHandler(1)).build();

        KeyShortcut copyTM3Shortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_3))
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_3))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.copyFromTM(3))
                        .setHandler(new CopyTMKeyShortcutHandler(2)).build();

        KeyShortcut copyTM4Shortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_4))
                        .addKey(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_4))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.copyFromTM(4))
                        .setHandler(new CopyTMKeyShortcutHandler(3)).build();
        keyShortcutPresenter.register(copyTM1Shortcut);
        keyShortcutPresenter.register(copyTM2Shortcut);
        keyShortcutPresenter.register(copyTM3Shortcut);
        keyShortcutPresenter.register(copyTM4Shortcut);
    }

    protected void registerNavigationKeys(
            final TargetContentsPresenter targetContentsPresenter) {
        KeyShortcut moveNextShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN))
                        .addKey(new Keys(Keys.ALT_KEY, 'K'))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.moveToNextRow())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setPreventDefault(true)
                        .setHandler(new KeyShortcutEventHandler() {
                            @Override
                            public void onKeyShortcut(KeyShortcutEvent event) {
                                targetContentsPresenter.moveToNextEntry();
                            }
                        }).build();
        keyShortcutPresenter.register(moveNextShortcut);

        KeyShortcut movePreviousShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP))
                        .addKey(new Keys(Keys.ALT_KEY, 'J'))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.moveToPreviousRow())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(new KeyShortcutEventHandler() {
                            @Override
                            public void onKeyShortcut(KeyShortcutEvent event) {
                                targetContentsPresenter.moveToPreviousEntry();
                            }
                        }).build();
        keyShortcutPresenter.register(movePreviousShortcut);

        nextStateShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEDOWN))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.nextIncomplete())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(new KeyShortcutEventHandler() {
                            @Override
                            public void onKeyShortcut(KeyShortcutEvent event) {
                                targetContentsPresenter
                                    .savePendingChangesIfApplicable();
                                eventBus.fireEvent(new NavTransUnitEvent(
                                    NextState));
                            }
                        }).build();
        keyShortcutPresenter.register(nextStateShortcut);

        prevStateShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEUP))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.prevIncomplete())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(new KeyShortcutEventHandler() {
                            @Override
                            public void onKeyShortcut(KeyShortcutEvent event) {
                                targetContentsPresenter
                                    .savePendingChangesIfApplicable();
                                eventBus.fireEvent(new NavTransUnitEvent(
                                    PrevState));
                            }
                        }).build();
        keyShortcutPresenter.register(prevStateShortcut);
    }

    protected void registerEditorActionKeys(
            final TargetContentsPresenter targetContentsPresenter) {
        // Register shortcut CTRL+S to save as fuzzy
        KeyShortcut saveFuzzyShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_KEY, 'S'))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.saveAsFuzzy())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(new KeyShortcutEventHandler() {
                            @Override
                            public void onKeyShortcut(KeyShortcutEvent event) {
                                targetContentsPresenter
                                        .saveAsFuzzy(targetContentsPresenter
                                                .getCurrentTransUnitIdOrNull());
                            }
                        }).build();
        keyShortcutPresenter.register(saveFuzzyShortcut);

        KeyShortcutEventHandler saveAsApprovedKeyShortcutHandler =
                new KeyShortcutEventHandler() {
                    @Override
                    public void onKeyShortcut(KeyShortcutEvent event) {
                        targetContentsPresenter.checkConfirmationBeforeSave();
                    }
                };

        KeyShortcut ctrlEnterShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.saveAsTranslated())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(saveAsApprovedKeyShortcutHandler).build();
        keyShortcutPresenter.register(ctrlEnterShortcut);
        enterSavesApprovedShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.saveAsTranslated())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(saveAsApprovedKeyShortcutHandler).build();
        if (configHolder.getState().isEnterSavesApproved()) {
            enterSavesApprovedHandlerRegistration =
                    keyShortcutPresenter.register(enterSavesApprovedShortcut);
        }

        KeyShortcut copySourceShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, 'G'))
                        .addAttentionKey(new Keys('G'))
                        .setContext(ShortcutContext.Edit)
                        .setDescription(messages.copyFromSource())
                        .setStopPropagation(true).setPreventDefault(true)
                        .setHandler(new KeyShortcutEventHandler() {
                            @Override
                            public void onKeyShortcut(KeyShortcutEvent event) {
                                targetContentsPresenter
                                        .copySourceForActiveRow();
                            }
                        }).build();
        keyShortcutPresenter.register(copySourceShortcut);
    }

    @Override
    public void onUserConfigChanged(UserConfigChangeEvent event) {
        if (event.getView() == MainView.Editor) {
            UserConfigHolder.ConfigurationState oldState = configuration;
            configuration = configHolder.getState();

            // If some config hasn't changed or not relevant in
            // this context, don't bother doing anything
            changeEnterSavesApproved(oldState);
            changeNavShortcutDescription(oldState);
        }
    }

    private void changeEnterSavesApproved(
            UserConfigHolder.ConfigurationState oldState) {
        if (oldState.isEnterSavesApproved() != configuration
                .isEnterSavesApproved()) {
            boolean enterSavesApproved = configuration.isEnterSavesApproved();
            if (enterSavesApproved) {
                enterSavesApprovedHandlerRegistration =
                        keyShortcutPresenter
                                .register(enterSavesApprovedShortcut);
            } else {
                if (enterSavesApprovedHandlerRegistration != null) {
                    enterSavesApprovedHandlerRegistration.removeHandler();
                }
            }
        }
    }

    private void changeNavShortcutDescription(
            UserConfigHolder.ConfigurationState oldState) {
        if (oldState.getNavOption() != configuration.getNavOption()) {
            switch (configuration.getNavOption()) {
            case FUZZY_UNTRANSLATED:
                nextStateShortcut.setDescription(messages.nextIncomplete());
                prevStateShortcut.setDescription(messages.nextIncomplete());
                break;
            case FUZZY:
                nextStateShortcut.setDescription(messages.nextDraft());
                prevStateShortcut.setDescription(messages.prevDraft());
                break;
            case UNTRANSLATED:
                nextStateShortcut.setDescription(messages.nextUntranslated());
                prevStateShortcut.setDescription(messages.prevUntranslated());
                break;
            }
        }
    }

    public void enableEditContext() {
        keyShortcutPresenter.setContextActive(ShortcutContext.Edit, true);
        keyShortcutPresenter
                .setContextActive(ShortcutContext.Navigation, false);
    }

    public void enableNavigationContext() {
        keyShortcutPresenter.setContextActive(ShortcutContext.Edit, false);
        keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
    }

    private class CopyTMKeyShortcutHandler implements KeyShortcutEventHandler {
        private int tmIndex;

        private CopyTMKeyShortcutHandler(int tmIndex) {
            this.tmIndex = tmIndex;
        }

        @Override
        public void onKeyShortcut(KeyShortcutEvent event) {
            eventBus.fireEvent(new TransMemoryShortcutCopyEvent(tmIndex));
        }
    }
}

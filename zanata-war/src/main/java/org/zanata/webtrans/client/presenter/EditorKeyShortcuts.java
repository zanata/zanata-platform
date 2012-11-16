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
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class EditorKeyShortcuts implements UserConfigChangeHandler
{
   private final KeyShortcutPresenter keyShortcutPresenter;
   private final EventBus eventBus;
   private final UserConfigHolder configHolder;
   private final TableEditorMessages messages;

   private KeyShortcut enterSavesApprovedShortcut;
   private KeyShortcut nextStateShortcut;
   private KeyShortcut prevStateShortcut;
   private KeyShortcut escClosesEditorShortcut;

   private HandlerRegistration enterSavesApprovedHandlerRegistration;
   private HandlerRegistration escClosesEditorHandlerRegistration;
   private UserConfigHolder.ConfigurationState configuration;

   @Inject
   public EditorKeyShortcuts(KeyShortcutPresenter keyShortcutPresenter, EventBus eventBus, UserConfigHolder configHolder, TableEditorMessages messages)
   {
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.eventBus = eventBus;
      this.configHolder = configHolder;
      this.messages = messages;

      configuration = configHolder.getState();
      eventBus.addHandler(UserConfigChangeEvent.TYPE, this);
   }

   public void registerKeys(TargetContentsPresenter targetContentsPresenter)
   {
      registerCopyTMKeys();
      registerNavigationKeys(targetContentsPresenter);
      registerEditorActionKeys(targetContentsPresenter);
   }

   protected void registerCopyTMKeys()
   {
      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_1), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_1)), ShortcutContext.Edit, messages.copyFromTM(1), new CopyTMKeyShortcutHandler(0)));
      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_2), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_2)), ShortcutContext.Edit, messages.copyFromTM(2), new CopyTMKeyShortcutHandler(1)));
      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_3), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_3)), ShortcutContext.Edit, messages.copyFromTM(3), new CopyTMKeyShortcutHandler(2)));
      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_4), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_4)), ShortcutContext.Edit, messages.copyFromTM(4), new CopyTMKeyShortcutHandler(3)));
   }

   protected void registerNavigationKeys(final TargetContentsPresenter targetContentsPresenter)
   {
      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN), new Keys(Keys.ALT_KEY, 'K')), ShortcutContext.Edit, messages.moveToNextRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.moveToNextEntry();
         }
      }));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP), new Keys(Keys.ALT_KEY, 'J')), ShortcutContext.Edit, messages.moveToPreviousRow(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.moveToPreviousEntry();
         }
      }));

      // Register shortcut ALT+(PageDown) to move next state entry - if modal
      // navigation is enabled
      nextStateShortcut = new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEDOWN), ShortcutContext.Edit, messages.nextFuzzyOrUntranslated(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            eventBus.fireEvent(new NavTransUnitEvent(NextState));
         }
      });
      keyShortcutPresenter.register(nextStateShortcut);

      // Register shortcut ALT+(PageUp) to move previous state entry - if modal
      // navigation is enabled
      prevStateShortcut = new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEUP), ShortcutContext.Edit, messages.prevFuzzyOrUntranslated(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            eventBus.fireEvent(new NavTransUnitEvent(PrevState));
         }
      });
      keyShortcutPresenter.register(prevStateShortcut);
   }

   protected void registerEditorActionKeys(final TargetContentsPresenter targetContentsPresenter)
   {
      // Register shortcut CTRL+S to save as fuzzy
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.CTRL_KEY, 'S'), ShortcutContext.Edit, messages.saveAsFuzzy(), KeyShortcut.KeyEvent.KEY_DOWN, true, true, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.saveAsFuzzy(targetContentsPresenter.getCurrentTransUnitIdOrNull());
         }
      }));

      KeyShortcutEventHandler saveAsApprovedKeyShortcutHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.saveAsApprovedAndMoveNext(targetContentsPresenter.getCurrentTransUnitIdOrNull(), true);
         }
      };

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER), ShortcutContext.Edit, messages.saveAsApproved(), KeyShortcut.KeyEvent.KEY_DOWN, true, true, saveAsApprovedKeyShortcutHandler));

      enterSavesApprovedShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Edit, messages.saveAsApproved(), KeyShortcut.KeyEvent.KEY_DOWN, true, true, saveAsApprovedKeyShortcutHandler);

      if (configHolder.isEnterSavesApproved())
      {
         enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
      }

      escClosesEditorShortcut = new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ESCAPE), ShortcutContext.Edit, messages.cancelChanges(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (!keyShortcutPresenter.getDisplay().isShowing())
            {
               targetContentsPresenter.onCancel(targetContentsPresenter.getCurrentTransUnitIdOrNull());
            }
         }
      });

     escClosesEditorHandlerRegistration = keyShortcutPresenter.register(escClosesEditorShortcut);

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'G'), ShortcutContext.Edit, messages.copyFromSource(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.copySourceForActiveRow();
         }
      }));
   }

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      if (event.getView() == MainView.Editor)
      {
         UserConfigHolder.ConfigurationState oldState = configuration;
         configuration = configHolder.getState();

         // If some config hasn't changed or not relevant in
         // this context, don't bother doing anything
         changeEnterSavesApproved(oldState);
         changeNavShortcutDescription(oldState);
      }
   }

   private void changeEnterSavesApproved(UserConfigHolder.ConfigurationState oldState)
   {
      if (oldState.isEnterSavesApproved() != configuration.isEnterSavesApproved())
      {
         boolean enterSavesApproved = configuration.isEnterSavesApproved();
         if (enterSavesApproved)
         {
            enterSavesApprovedHandlerRegistration = keyShortcutPresenter.register(enterSavesApprovedShortcut);
         }
         else
         {
            if (enterSavesApprovedHandlerRegistration != null)
            {
               enterSavesApprovedHandlerRegistration.removeHandler();
            }
         }
      }
   }

   private void changeNavShortcutDescription(UserConfigHolder.ConfigurationState oldState)
   {
      if (oldState.getNavOption() != configuration.getNavOption())
      {
         switch (configuration.getNavOption())
         {
            case FUZZY_UNTRANSLATED:
               nextStateShortcut.setDescription(messages.nextFuzzyOrUntranslated());
               prevStateShortcut.setDescription(messages.nextFuzzyOrUntranslated());
               break;
            case FUZZY:
               nextStateShortcut.setDescription(messages.nextFuzzy());
               prevStateShortcut.setDescription(messages.prevFuzzy());
               break;
            case UNTRANSLATED:
               nextStateShortcut.setDescription(messages.nextUntranslated());
               prevStateShortcut.setDescription(messages.prevUntranslated());
               break;
         }
      }
   }

   public void enableEditContext()
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.Edit, true);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, false);
   }

   public void enableNavigationContext()
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.Edit, false);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
   }

   private class CopyTMKeyShortcutHandler implements KeyShortcutEventHandler
   {
      private int tmIndex;

      private CopyTMKeyShortcutHandler(int tmIndex)
      {
         this.tmIndex = tmIndex;
      }

      @Override
      public void onKeyShortcut(KeyShortcutEvent event)
      {
         eventBus.fireEvent(new TransMemoryShortcutCopyEvent(tmIndex));
      }
   }
}

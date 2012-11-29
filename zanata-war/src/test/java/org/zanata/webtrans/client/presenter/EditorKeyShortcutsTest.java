package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.event.dom.client.KeyCodes;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class EditorKeyShortcutsTest
{
   private EditorKeyShortcuts keyShortcuts;
   @Mock
   private KeyShortcutPresenter keyShortcutPresenter;
   @Mock
   private EventBus eventBus;
   private UserConfigHolder configHolder;
   @Mock
   private TableEditorMessages messages;
   @Mock
   private TargetContentsPresenter targetContentsPresenter;
   @Captor
   private ArgumentCaptor<KeyShortcut> keyShortcutCaptor;
   @Mock
   private KeyShortcutPresenter.Display keyShortcutDisplay;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      configHolder = new UserConfigHolder();
      keyShortcuts = new EditorKeyShortcuts(keyShortcutPresenter, eventBus, configHolder, messages);

      verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, keyShortcuts);
   }

   @Test
   public void testRegisterKeys() throws Exception
   {
      // Given:
      EditorKeyShortcuts spyKeyShortcuts = spy(keyShortcuts);
      doNothing().when(spyKeyShortcuts).registerCopyTMKeys();
      doNothing().when(spyKeyShortcuts).registerEditorActionKeys(targetContentsPresenter);
      doNothing().when(spyKeyShortcuts).registerNavigationKeys(targetContentsPresenter);

      // When:
      spyKeyShortcuts.registerKeys(targetContentsPresenter);

      // Then:
      verify(spyKeyShortcuts).registerCopyTMKeys();
      verify(spyKeyShortcuts).registerEditorActionKeys(targetContentsPresenter);
      verify(spyKeyShortcuts).registerNavigationKeys(targetContentsPresenter);
   }

   @Test
   public void testRegisterCopyTMKeys()
   {
      when(messages.copyFromTM(1)).thenReturn("copy from tm 1");
      when(messages.copyFromTM(2)).thenReturn("copy from tm 2");
      when(messages.copyFromTM(3)).thenReturn("copy from tm 3");
      when(messages.copyFromTM(4)).thenReturn("copy from tm 4");

      keyShortcuts.registerCopyTMKeys();

      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> keys = keyShortcutCaptor.getAllValues();
      assertKeys(keys.get(0), "copy from tm 1", false, false, new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_1), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_1));
      assertKeys(keys.get(1), "copy from tm 2", false, false, new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_2), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_2));
      assertKeys(keys.get(2), "copy from tm 3", false, false, new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_3), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_3));
      assertKeys(keys.get(3), "copy from tm 4", false, false, new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_4), new Keys(Keys.CTRL_ALT_KEYS, Keys.KEY_NUM_4));
   }

   @Test
   public void testCopyTMKeyHandler()
   {
      keyShortcuts.registerCopyTMKeys();
      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> keys = keyShortcutCaptor.getAllValues();
      KeyShortcut copy3TM = keys.get(3);
      ArgumentCaptor<TransMemoryShortcutCopyEvent> eventCaptor = ArgumentCaptor.forClass(TransMemoryShortcutCopyEvent.class);

      copy3TM.getHandler().onKeyShortcut(null);

      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getIndex(), Matchers.is(3));
   }

   @Test
   public void testRegisterNavigationKeys()
   {
      when(messages.moveToNextRow()).thenReturn("next entry");
      when(messages.moveToPreviousRow()).thenReturn("previous entry");
      when(messages.nextFuzzyOrUntranslated()).thenReturn("next fuzzy or untranslated");
      when(messages.prevFuzzyOrUntranslated()).thenReturn("previous fuzzy or untranslated");

      keyShortcuts.registerNavigationKeys(targetContentsPresenter);

      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> keys = keyShortcutCaptor.getAllValues();
      assertKeys(keys.get(0), "next entry", true, true, new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN), new Keys(Keys.ALT_KEY, 'K'));
      assertKeys(keys.get(1), "previous entry", true, true, new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP), new Keys(Keys.ALT_KEY, 'J'));
      assertKeys(keys.get(2), "next fuzzy or untranslated", true, true, new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEDOWN));
      assertKeys(keys.get(3), "previous fuzzy or untranslated", true, true, new Keys(Keys.ALT_KEY, KeyCodes.KEY_PAGEUP));
   }

   @Test
   public void testNextEntryKeyHandler()
   {
      keyShortcuts.registerNavigationKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      KeyShortcut nextEntry = keyShortcutCaptor.getAllValues().get(0);

      nextEntry.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).moveToNextEntry();
   }

   @Test
   public void testPreviousEntryKeyHandler()
   {
      keyShortcuts.registerNavigationKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      KeyShortcut prevEntry = keyShortcutCaptor.getAllValues().get(1);

      prevEntry.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).moveToPreviousEntry();
   }

   @Test
   public void testNextStateKeyHandler()
   {
      keyShortcuts.registerNavigationKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      KeyShortcut nextState = keyShortcutCaptor.getAllValues().get(2);
      ArgumentCaptor<NavTransUnitEvent> eventCaptor = ArgumentCaptor.forClass(NavTransUnitEvent.class);

      nextState.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(NavTransUnitEvent.NavigationType.NextState));
   }

   @Test
   public void testPreviousStateKeyHandler()
   {
      keyShortcuts.registerNavigationKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      KeyShortcut prevState = keyShortcutCaptor.getAllValues().get(3);
      ArgumentCaptor<NavTransUnitEvent> eventCaptor = ArgumentCaptor.forClass(NavTransUnitEvent.class);

      prevState.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(NavTransUnitEvent.NavigationType.PrevState));
   }

   @Test
   public void testRegisterEditorActionKeys()
   {
      // by default user config settings
      when(messages.saveAsFuzzy()).thenReturn("save fuzzy");
      when(messages.saveAsApproved()).thenReturn("save approved");
      when(messages.copyFromSource()).thenReturn("copy from source");
      
      keyShortcuts.registerEditorActionKeys(targetContentsPresenter);

      verify(keyShortcutPresenter, times(3)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> keys = keyShortcutCaptor.getAllValues();
      assertKeys(keys.get(0), "save fuzzy", true, true, new Keys(Keys.CTRL_KEY, 'S'));
      assertKeys(keys.get(1), "save approved", true, true, new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER));
      assertKeys(keys.get(2), "copy from source", false, false, new Keys(Keys.ALT_KEY, 'G'));
   }

   @Test
   public void registerEditorActionKeysAfterChangeUserConfig()
   {
      // enter and esc now active
      when(messages.saveAsFuzzy()).thenReturn("save fuzzy");
      when(messages.saveAsApproved()).thenReturn("save approved");
      when(messages.copyFromSource()).thenReturn("copy from source");
      
      configHolder.setEnterSavesApproved(true);

      keyShortcuts.registerEditorActionKeys(targetContentsPresenter);

      verify(keyShortcutPresenter, times(4)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> keys = keyShortcutCaptor.getAllValues();
      assertKeys(keys.get(0), "save fuzzy", true, true, new Keys(Keys.CTRL_KEY, 'S'));
      assertKeys(keys.get(1), "save approved", true, true, new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER));
      assertKeys(keys.get(2), "save approved", true, true, new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER));
      assertKeys(keys.get(3), "copy from source", false, false, new Keys(Keys.ALT_KEY, 'G'));
   }

   @Test
   public void testSaveFuzzyKeyHandler()
   {
      keyShortcuts.registerEditorActionKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, atLeastOnce()).register(keyShortcutCaptor.capture());
      KeyShortcut keys = keyShortcutCaptor.getAllValues().get(0);

      keys.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).saveAsFuzzy(targetContentsPresenter.getCurrentTransUnitIdOrNull());
   }

   @Test
   public void testSaveApprovedKeyHandler()
   {
      keyShortcuts.registerEditorActionKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, atLeastOnce()).register(keyShortcutCaptor.capture());
      KeyShortcut keys = keyShortcutCaptor.getAllValues().get(1);

      keys.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).saveAsApprovedAndMoveNext(targetContentsPresenter.getCurrentTransUnitIdOrNull());
   }

   @Test
   public void testCopySourceKeyHandler()
   {
      keyShortcuts.registerEditorActionKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, atLeastOnce()).register(keyShortcutCaptor.capture());
      KeyShortcut keys = keyShortcutCaptor.getAllValues().get(2);

      keys.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).copySourceForActiveRow();
   }

   @Test
   public void testEnterSaveKeyHandler()
   {
      configHolder.setEnterSavesApproved(true);
      keyShortcuts.registerEditorActionKeys(targetContentsPresenter);
      verify(keyShortcutPresenter, atLeastOnce()).register(keyShortcutCaptor.capture());
      KeyShortcut keys = keyShortcutCaptor.getAllValues().get(2);

      keys.getHandler().onKeyShortcut(null);

      verify(targetContentsPresenter).saveAsApprovedAndMoveNext(targetContentsPresenter.getCurrentTransUnitIdOrNull());
   }

   @Test
   public void testOnUserConfigChangedToEnterSaves() throws Exception
   {
      // Given: change user config enter save approved
      when(messages.saveAsApproved()).thenReturn("enter save as approved");
      keyShortcuts.registerKeys(targetContentsPresenter);
      configHolder.setEnterSavesApproved(true);

      // When:
      keyShortcuts.onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

      // Then:
      verify(keyShortcutPresenter, atLeastOnce()).register(keyShortcutCaptor.capture());
      List<KeyShortcut> allKeys = keyShortcutCaptor.getAllValues();
      KeyShortcut shortcut = allKeys.get(allKeys.size() - 1); // last key

      assertKeys(shortcut, "enter save as approved", true, true, new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER));
   }

   @Test
   public void testOnUserConfigChangedNavOption() throws Exception
   {
      // Given: change user config esc to close edit
      when(messages.nextFuzzy()).thenReturn("next fuzzy");
      when(messages.prevFuzzy()).thenReturn("prev fuzzy");
      keyShortcuts.registerNavigationKeys(targetContentsPresenter);
      configHolder.setNavOption(NavOption.FUZZY);

      // When:
      keyShortcuts.onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

      // Then:
      verify(keyShortcutPresenter, atLeastOnce()).register(keyShortcutCaptor.capture());
      List<KeyShortcut> allKeys = keyShortcutCaptor.getAllValues();
      KeyShortcut nextState = allKeys.get(2);
      KeyShortcut prevState = allKeys.get(3);

      assertThat(nextState.getDescription(), Matchers.equalTo("next fuzzy"));
      assertThat(prevState.getDescription(), Matchers.equalTo("prev fuzzy"));
   }

   private static void assertKeys(KeyShortcut shortcut, String description, boolean isStopPropagation, boolean isPreventDefault, Keys... keys)
   {
      assertThat(shortcut.getAllKeys(), Matchers.containsInAnyOrder(keys));
      assertThat(shortcut.getContext(), Matchers.equalTo(ShortcutContext.Edit));
      assertThat(shortcut.getDescription(), Matchers.equalTo(description));
      assertThat(shortcut.getKeyEvent(), Matchers.equalTo(KeyShortcut.KeyEvent.KEY_DOWN));
      assertThat(shortcut.isStopPropagation(), Matchers.equalTo(isStopPropagation));
      assertThat(shortcut.isPreventDefault(), Matchers.equalTo(isPreventDefault));
   }

   @Test
   public void testEnableEditContext() throws Exception
   {
      keyShortcuts.enableEditContext();

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, true);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, false);
   }

   @Test
   public void testEnableNavigationContext() throws Exception
   {
      keyShortcuts.enableNavigationContext();

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, false);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, true);
   }
}

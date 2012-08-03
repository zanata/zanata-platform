/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.editor.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.TranslationHistoryPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ValidationMessagePanelDisplay;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.collect.Lists;
import com.google.inject.Provider;

@Test(groups = { "unit-tests" })
public class TargetContentsPresenterTest
{
   private TargetContentsPresenter presenter;

   @Mock private EventBus eventBus;
   @Mock private TableEditorMessages tableEditorMessages;
   @Mock private SourceContentsPresenter sourceContentPresenter;
   @Mock private KeyShortcutPresenter keyShortcutPresenter;
   @Mock private NavigationMessages navMessages;
   @Mock private UserWorkspaceContext userWorkspaceContext;
   @Mock private TargetContentsDisplay display;
   @Mock private ValidationMessagePanelDisplay validationPanel;
   @Mock
   private ToggleEditor editor, editor2, editor3;
   @Mock
   private Identity identity;
   @Mock private TransUnit transUnit;
   @Mock private UserConfigHolder configHolder;

   private final ArrayList<String> targetContents = Lists.newArrayList("", "");
   @Captor private ArgumentCaptor<RunValidationEvent> runValidationEventCaptor;
   @Captor private ArgumentCaptor<NotificationEvent> notificationEventCaptor;

   @Mock
   private UserSessionService sessionService;
   @Mock
   private Provider<TargetContentsDisplay> displayProvider;

   @Mock
   private TranslationHistoryPresenter historyPresenter;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TargetContentsPresenter(displayProvider, identity, eventBus, tableEditorMessages, sourceContentPresenter, sessionService, configHolder, userWorkspaceContext, validationPanel, keyShortcutPresenter, historyPresenter);

      verify(eventBus).addHandler(UserConfigChangeEvent.getType(), presenter);
      verify(eventBus).addHandler(RequestValidationEvent.getType(), presenter);
      verify(eventBus).addHandler(InsertStringInEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(CopyDataToEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(EnableModalNavigationEvent.getType(), presenter);

      verify(display).setListener(presenter);
   }

   private void verifyRevealDisplay()
   {
      verify(keyShortcutPresenter, atLeastOnce()).setContextActive(ShortcutContext.Edit, true);
      verify(keyShortcutPresenter, atLeastOnce()).setContextActive(ShortcutContext.Navigation, false);
   }

   @Test
   public void canSetToViewMode()
   {
//      presenter.setToViewMode();

//      verify(display).setToView();
      verify(display).showButtons(false);
   }

   @Test
   public void canSetValueToDisplay()
   {
      //given NOT read only mode, enter NOT as save
      String buttonTitle = "Save (Ctrl + Enter)";
      String findMessages = "abc";
      when(transUnit.getTargets()).thenReturn(targetContents);
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
      when(configHolder.isEnterSavesApproved()).thenReturn(false);
      when(navMessages.editSaveShortcut()).thenReturn(buttonTitle);

      //when selecting row 1
      TargetContentsDisplay result = presenter.setValue(transUnit);

      assertThat(result, sameInstance(display));
      verify(display).setValue(transUnit);
      verify(display).setFindMessage(findMessages);
      verifyNoMoreInteractions(display);
   }

   @Test
   public void canValidate()
   {
      when(editor.getIndex()).thenReturn(0);
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");
      when(editor.getText()).thenReturn("target");

      presenter.validate(editor);

      verify(eventBus).fireEvent(runValidationEventCaptor.capture());
      RunValidationEvent event = runValidationEventCaptor.getValue();
      assertThat(event.getSourceContent(), equalTo("source"));
      assertThat(event.getTarget(), equalTo("target"));
      assertThat(event.isFireNotification(), equalTo(false));
   }

   @Test
   public void willNotValidateIfEditorIsNotCurrent()
   {
      when(editor.getIndex()).thenReturn(99); //current editor is not the focused one

      presenter.validate(editor);

      verify(editor).getIndex();
      verifyNoMoreInteractions(editor);
   }

   @Test
   public void canSaveAsFuzzy()
   {
      when(display.getNewTargets()).thenReturn(targetContents);
      presenter.saveAsFuzzy();

      ArgumentCaptor<TransUnitSaveEvent> captor = ArgumentCaptor.forClass(TransUnitSaveEvent.class);
      verify(eventBus).fireEvent(captor.capture());
      TransUnitSaveEvent event = captor.getValue();
      assertThat(event.getTargets(), Matchers.<List<String>>equalTo(targetContents));
      assertThat(event.getStatus(), equalTo(ContentState.NeedReview));
   }

   @Test
   public void canCopySource()
   {
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");
      when(configHolder.isDisplayButtons()).thenReturn(false);

      presenter.copySource(editor);

      verify(editor).setTextAndValidate("source");
      verify(editor).setViewMode(ToggleEditor.ViewMode.EDIT);
      verify(display).showButtons(false);
      verify(editor).autoSize();
      verify(editor).setFocus();
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
      verifyRevealDisplay();
   }

   @Test
   public void isDisplayButtonsReturnFromUserConfig()
   {
      when(configHolder.isDisplayButtons()).thenReturn(true);
      assertThat(presenter.isDisplayButtons(), Matchers.equalTo(true));

      when(configHolder.isDisplayButtons()).thenReturn(false);
      assertThat(presenter.isDisplayButtons(), Matchers.equalTo(false));
   }

   @Test
   public void canGetNewTargets()
   {
      presenter.showEditors(0, transUnit.getId());
      when(display.getNewTargets()).thenReturn(targetContents);

      ArrayList<String> result = presenter.getNewTargets();

      MatcherAssert.assertThat(result, Matchers.sameInstance(targetContents));
   }
   
   @Test
   public void canSetValidationMessagePanel()
   {
      presenter.setValidationMessagePanel(editor);

      verify(validationPanel).clear();
      verify(editor).addValidationMessagePanel(validationPanel);
   }
   @Test
   public void canChangeViewOnUserConfigChange()
   {
      when(configHolder.isEnterSavesApproved()).thenReturn(true);

      presenter.onValueChanged(UserConfigChangeEvent.EVENT);

      verify(display).showButtons(configHolder.isDisplayButtons());
   }

   @Test
   public void onRequestValidationWillNotFireRunValidationEventIfNotEditing()
   {
      //given current display is null

      presenter.onRequestValidation(new RequestValidationEvent());

      verifyNoMoreInteractions(eventBus);

   }

   @Test
   public void onRequestValidationWillFireRunValidationEventIfItsEditing()
   {
      //given current display has one editor and current editor has target content
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      when(editor.getIndex()).thenReturn(0);
      when(editor.getText()).thenReturn("target");

      presenter.onRequestValidation(new RequestValidationEvent());

      verify(eventBus).fireEvent(runValidationEventCaptor.capture());//one in showEditor() one in onRequestValidation()
      MatcherAssert.assertThat(runValidationEventCaptor.getValue().getTarget(), Matchers.equalTo("target"));
      verifyRevealDisplay();
   }

   private void givenCurrentEditorsAs(ToggleEditor... currentEditors)
   {
      when(display.getEditors()).thenReturn(Lists.newArrayList(currentEditors));
      when(display.isEditing()).thenReturn(true);
      presenter.showEditors(0, transUnit.getId());
   }

   @Test
   public void onCancelCanResetTextBack()
   {
      presenter.onCancel();

      verify(eventBus).fireEvent(TransUnitSaveEvent.CANCEL_EDIT_EVENT);
   }

   @Test
   public void testOnInsertString()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      when(editor.getIndex()).thenReturn(0);
      givenCurrentEditorsAs(editor);

      presenter.onInsertString(new InsertStringInEditorEvent("", "suggestion"));

      verify(editor).insertTextInCursorPosition("suggestion");
      verify(eventBus, times(2)).fireEvent(isA(RunValidationEvent.class));
      verify(eventBus, atLeastOnce()).fireEvent(notificationEventCaptor.capture());
      MatcherAssert.assertThat(notificationEventCaptor.getValue().getMessage(), Matchers.equalTo("copied"));
   }

   @Test
   public void testOnTransMemoryCopy()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      givenCurrentEditorsAs(editor);

      // TODO update for plurals
      presenter.onTransMemoryCopy(new CopyDataToEditorEvent(Arrays.asList("target")));

      verify(editor).setTextAndValidate("target");
      verify(eventBus, atLeastOnce()).fireEvent(notificationEventCaptor.capture());
      MatcherAssert.assertThat(notificationEventCaptor.getValue().getMessage(), Matchers.equalTo("copied"));
   }

   @Test
   public void testShowEditors()
   {

   }

   @Test
   public void testSaveAsApprovedAndMoveNext()
   {

   }

   @Test
   public void testSaveAsApprovedAndMovePrevious() 
   {

   }

   @Test
   public void testOnEditorKeyDown() 
   {

   }
}

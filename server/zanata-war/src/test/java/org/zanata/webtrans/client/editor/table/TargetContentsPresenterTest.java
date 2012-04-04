/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.client.editor.table;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ValidationMessagePanelDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;

@Test(groups = { "unit-tests" })
public class TargetContentsPresenterTest
{
   public static final int PAGE_SIZE = 2;
   private TargetContentsPresenter presenter;

   @Mock private Provider<TargetContentsDisplay> displayProvider;
   @Mock private EventBus eventBus;
   @Mock private TableEditorMessages tableEditorMessages;
   @Mock private SourceContentsPresenter sourceContentPresenter;
   @Mock private NavigationMessages navMessages;
   @Mock private WorkspaceContext workspaceContext;
   @Mock private Scheduler scheduler;
   @Mock private TargetContentsDisplay display1;
   @Mock private TargetContentsDisplay display2;
   @Mock private ValidationMessagePanelDisplay validationPanel;
   @Mock
   private ToggleEditor editor, editor2, editor3;
   @Mock private TransUnit transUnit;
   @Mock private UserConfigHolder configHolder;
   @Mock private TransUnitsEditModel cellEditor;
   private final ArrayList<String> targetContents = Lists.newArrayList("", "");
   @Captor private ArgumentCaptor<RunValidationEvent> runValidationEventCaptor;
   @Captor private ArgumentCaptor<NotificationEvent> notificationEventCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TargetContentsPresenter(displayProvider, eventBus, tableEditorMessages, sourceContentPresenter, configHolder, navMessages, workspaceContext, scheduler, validationPanel);

      verify(eventBus).addHandler(UserConfigChangeEvent.getType(), presenter);
      verify(eventBus).addHandler(RequestValidationEvent.getType(), presenter);
      verify(eventBus).addHandler(InsertStringInEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(CopyDataToEditorEvent.getType(), presenter);

      presenter.setCellEditor(cellEditor);

      when(displayProvider.get()).thenReturn(display1).thenReturn(display2);
      presenter.initWidgets(PAGE_SIZE);

      verify(display1).setListener(presenter);
      verify(display2).setListener(presenter);
   }

   @Test
   public void canSetToViewMode() 
   {
      //given show editor at row 0
      presenter.showEditors(0, -1);

      presenter.setToViewMode();

      verify(display1).setToView();
      verifyZeroInteractions(display2);
   }

   @Test
   public void canGetNextTargetContentsDisplay() 
   {
      //given NOT read only mode, enter NOT as save
      String buttonTitle = "Save (Ctrl + Enter)";
      String findMessages = "abc";
      when(transUnit.getTargets()).thenReturn(targetContents);
      when(workspaceContext.isReadOnly()).thenReturn(false);
      when(configHolder.isButtonEnter()).thenReturn(false);
      when(navMessages.editSaveShortcut()).thenReturn(buttonTitle);

      //when selecting row 1
      TargetContentsDisplay result = presenter.getNextTargetContentsDisplay(0, transUnit, findMessages);

      assertThat(result, sameInstance(display1));
      verify(display1).setTargets(targetContents);
      verify(display1).setFindMessage(findMessages);
      verify(display1).setSaveButtonTitle(buttonTitle);
      verifyNoMoreInteractions(display1);
      verifyZeroInteractions(display2);
   }

   @Test
   public void canGetNextTargetContentsDisplayWithDifferentButtonTitle() 
   {
      //given read only mode, enter as save option
      String buttonTitle = "Save (Enter)";
      String findMessages = "abc";
      when(transUnit.getTargets()).thenReturn(targetContents);
      when(workspaceContext.isReadOnly()).thenReturn(true);
      when(configHolder.isButtonEnter()).thenReturn(true);
      when(navMessages.editSaveWithEnterShortcut()).thenReturn(buttonTitle);

      //when selecting row 2
      TargetContentsDisplay result = presenter.getNextTargetContentsDisplay(1, transUnit, findMessages);

      assertThat(result, sameInstance(display2));
      verify(display2).setTargets(targetContents);
      verify(display2).setFindMessage(findMessages);
      verify(display2).setSaveButtonTitle(buttonTitle);
      verify(display2).showButtons(false);
      verifyNoMoreInteractions(display2);
      verifyZeroInteractions(display1);
   }

   @Test
   public void canValidate()
   {
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");
      when(editor.getText()).thenReturn("target");

      presenter.validate(editor);

      verify(eventBus).fireEvent(runValidationEventCaptor.capture());
      RunValidationEvent event = runValidationEventCaptor.getValue();
      assertThat(event.getSource(), equalTo("source"));
      assertThat(event.getTarget(), equalTo("target"));
      assertThat(event.isFireNotification(), equalTo(false));
   }

   @Test
   public void canSaveAsFuzzy()
   {
      presenter.saveAsFuzzy();

      verify(cellEditor).acceptFuzzyEdit();
   }

   @Test
   public void canCopySource()
   {
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");

      presenter.copySource(editor);

      verify(editor).setText("source");
      verify(editor).autoSize();
      verify(eventBus).fireEvent(isA(RunValidationEvent.class));
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
   }

   @Test
   public void toggleViewIsDeferredExecuted()
   {
      //given current display is at row 1
      ArgumentCaptor<Scheduler.ScheduledCommand> commandCaptor = ArgumentCaptor.forClass(Scheduler.ScheduledCommand.class);
      when(editor.getIndex()).thenReturn(99);
      presenter.showEditors(0, -1);

      presenter.toggleView(editor);

      verify(scheduler).scheduleDeferred(commandCaptor.capture());
      commandCaptor.getValue().execute();
      verify(display1).openEditorAndCloseOthers(99);
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
      presenter.showEditors(1, -1);
      when(display2.getNewTargets()).thenReturn(targetContents);

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
      when(configHolder.isButtonEnter()).thenReturn(true);

      presenter.onValueChanged(new UserConfigChangeEvent());

      verify(display1).showButtons(configHolder.isDisplayButtons());
      verify(display1).setSaveButtonTitle(navMessages.editSaveWithEnterShortcut());
      verify(display2).showButtons(configHolder.isDisplayButtons());
      verify(display2).setSaveButtonTitle(navMessages.editSaveWithEnterShortcut());
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
      //given current display is row 1 and current editor has target content
      givenCurrentEditorAs(editor);
      when(editor.getText()).thenReturn("target");

      presenter.onRequestValidation(new RequestValidationEvent());

      verify(eventBus).fireEvent(runValidationEventCaptor.capture());
      MatcherAssert.assertThat(runValidationEventCaptor.getValue().getTarget(), Matchers.equalTo("target"));
   }

   private void givenCurrentEditorAs(ToggleEditor currentEditor)
   {
      ArrayList<ToggleEditor> mockedList = Mockito.mock(ArrayList.class);
      when(display1.getEditors()).thenReturn(mockedList);
      when(mockedList.get(anyInt())).thenReturn(currentEditor);
      when(display1.isEditing()).thenReturn(true);
      presenter.showEditors(0, -1);
   }

   @Test
   public void canSaveAndMoveRow()
   {
      presenter.saveAndMoveRow(NavTransUnitEvent.NavigationType.NextEntry);

      verify(cellEditor).saveAndMoveRow(NavTransUnitEvent.NavigationType.NextEntry);
   }

   @Test
   public void onCancelCanResetTextBack()
   {
      when(display1.getEditors()).thenReturn(Lists.newArrayList(editor, editor2, editor3));
      presenter.showEditors(0, 1);
      when(cellEditor.getTargetCell()).thenReturn(transUnit);
      when(transUnit.getTargets()).thenReturn(Lists.newArrayList("a", "b", "c"));
      when(editor.getIndex()).thenReturn(1);

      presenter.onCancel(editor);

      verify(editor).setViewMode(ToggleEditor.ViewMode.VIEW);
      verify(editor).setText("b");
   }

   @Test
   public void onCancelCanSetTextBackToNull()
   {
      when(display1.getEditors()).thenReturn(Lists.newArrayList(editor, editor2, editor3));
      presenter.showEditors(0, 1);
      when(cellEditor.getTargetCell()).thenReturn(transUnit);
      when(transUnit.getTargets()).thenReturn(null);

      presenter.onCancel(editor);

      verify(editor).setViewMode(ToggleEditor.ViewMode.VIEW);
      verify(editor).setText(null);
   }

   @Test
   public void testOnInsertString()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      givenCurrentEditorAs(editor);

      presenter.onInsertString(new InsertStringInEditorEvent("", "suggestion"));

      verify(editor).insertTextInCursorPosition("suggestion");
      ArgumentCaptor<GwtEvent> eventArgumentCaptor = ArgumentCaptor.forClass(GwtEvent.class);
      verify(eventBus, times(2)).fireEvent(eventArgumentCaptor.capture());
      NotificationEvent notificationEvent = findEvent(eventArgumentCaptor, NotificationEvent.class);
      MatcherAssert.assertThat(notificationEvent.getMessage(), Matchers.equalTo("copied"));
      RunValidationEvent runValidationEvent = findEvent(eventArgumentCaptor, RunValidationEvent.class);
      MatcherAssert.assertThat(runValidationEvent, Matchers.notNullValue());
   }

   private <T> T findEvent(ArgumentCaptor<GwtEvent> eventArgumentCaptor, Class<T> clazz)
   {
      for (GwtEvent event : eventArgumentCaptor.getAllValues())
      {
         if (clazz.isAssignableFrom(event.getClass()))
         {
            return clazz.cast(event);
         }
      }
      throw new RuntimeException("can't find event type in captured values: " + clazz.getName());
   }

   @Test
   public void testOnTransMemoryCopy()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      givenCurrentEditorAs(editor);

      // TODO update for plurals
      presenter.onTransMemoryCopy(new CopyDataToEditorEvent(Arrays.asList("target")));

      verify(editor).setText("target");
      ArgumentCaptor<GwtEvent> eventArgumentCaptor = ArgumentCaptor.forClass(GwtEvent.class);
      verify(eventBus, times(2)).fireEvent(eventArgumentCaptor.capture());
      NotificationEvent notificationEvent = findEvent(eventArgumentCaptor, NotificationEvent.class);
      MatcherAssert.assertThat(notificationEvent.getMessage(), Matchers.equalTo("copied"));
      RunValidationEvent runValidationEvent = findEvent(eventArgumentCaptor, RunValidationEvent.class);
      MatcherAssert.assertThat(runValidationEvent, Matchers.notNullValue());
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

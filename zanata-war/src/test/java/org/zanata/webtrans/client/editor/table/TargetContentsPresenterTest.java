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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.UserColorService;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.ValidationMessagePanelDisplay;
import org.zanata.webtrans.shared.auth.Identity;
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
   @Mock
   private Identity identity;
   @Mock private TransUnit transUnit;
   @Mock private UserConfigHolder configHolder;
   @Mock private TransUnitsEditModel cellEditor;

   private final ArrayList<String> targetContents = Lists.newArrayList("", "");
   @Captor private ArgumentCaptor<RunValidationEvent> runValidationEventCaptor;
   @Captor private ArgumentCaptor<NotificationEvent> notificationEventCaptor;

   @Mock
   private UserColorService translatorColorService;

   @Mock
   private UserSessionService sessionService;


   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TargetContentsPresenter(displayProvider, identity, eventBus, tableEditorMessages, sourceContentPresenter, sessionService, configHolder, workspaceContext, scheduler, validationPanel, translatorColorService);

      verify(eventBus).addHandler(UserConfigChangeEvent.getType(), presenter);
      verify(eventBus).addHandler(RequestValidationEvent.getType(), presenter);
      verify(eventBus).addHandler(InsertStringInEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(CopyDataToEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(EnableModalNavigationEvent.getType(), presenter);

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
      verify(display1).showButtons(false);
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
      verify(display2).showButtons(false);
      verifyNoMoreInteractions(display2);
      verifyZeroInteractions(display1);
   }

   @Test
   public void canValidate()
   {
      givenCurrentEditorsAs(editor);
      when(editor.getIndex()).thenReturn(0);
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
      presenter.saveAsFuzzy();

      verify(cellEditor).acceptFuzzyEdit();
   }

   @Test
   public void canCopySource()
   {
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");
      presenter.showEditors(0, TargetContentsPresenter.NO_OPEN_EDITOR);

      presenter.copySource(editor);

      verify(editor).setTextAndValidate("source");
      verify(editor).setViewMode(ToggleEditor.ViewMode.EDIT);
      verify(display1).showButtons(true);
      verify(editor).autoSize();
      verify(editor).setFocus();
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
   }

   @Test
   public void toggleViewIsDeferredExecuted()
   {
      ArgumentCaptor<Scheduler.ScheduledCommand> commandCaptor = ArgumentCaptor.forClass(Scheduler.ScheduledCommand.class);
      when(editor.getIndex()).thenReturn(99);
      presenter.showEditors(0, TargetContentsPresenter.NO_OPEN_EDITOR);

      presenter.toggleView(editor);

      verify(scheduler).scheduleDeferred(commandCaptor.capture());
      commandCaptor.getValue().execute();

      verify(display1).focusEditor(99);
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
      presenter.showEditors(1, TargetContentsPresenter.NO_OPEN_EDITOR);
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
      verify(display2).showButtons(configHolder.isDisplayButtons());
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
      givenCurrentEditorsAs(editor);
      when(editor.getIndex()).thenReturn(0);
      when(editor.getText()).thenReturn("target");

      presenter.onRequestValidation(new RequestValidationEvent());

      verify(eventBus).fireEvent(runValidationEventCaptor.capture());
      MatcherAssert.assertThat(runValidationEventCaptor.getValue().getTarget(), Matchers.equalTo("target"));
   }

   private void givenCurrentEditorsAs(ToggleEditor... currentEditors)
   {
      when(display1.getEditors()).thenReturn(Lists.newArrayList(currentEditors));
      when(display1.isEditing()).thenReturn(true);
      presenter.showEditors(0, 0);
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
      givenCurrentEditorsAs(editor, editor2, editor3);
      when(cellEditor.getTargetCell()).thenReturn(transUnit);
      when(transUnit.getTargets()).thenReturn(Lists.newArrayList("a", "b", "c"));
      when(editor.getIndex()).thenReturn(0);
      when(editor2.getIndex()).thenReturn(1);
      when(editor3.getIndex()).thenReturn(2);

      presenter.onCancel();

      verify(display1).setToView();
      verify(editor).setTextAndValidate("a");
      verify(editor2).setTextAndValidate("b");
      verify(editor3).setTextAndValidate("c");
   }

   @Test
   public void onCancelCanSetTextBackToNull()
   {
      givenCurrentEditorsAs(editor, editor2, editor3);
      when(cellEditor.getTargetCell()).thenReturn(transUnit);
      when(transUnit.getTargets()).thenReturn(null);

      presenter.onCancel();

      verify(display1).setToView();
      verify(editor).setTextAndValidate(null);
      verify(editor2).setTextAndValidate(null);
      verify(editor3).setTextAndValidate(null);
   }

   @Test
   public void testOnInsertString()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      when(editor.getIndex()).thenReturn(0);
      givenCurrentEditorsAs(editor);

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

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
package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.model.TestFixture.makeTransUnit;

@Test(groups = { "unit-tests" })
public class TargetContentsPresenterTest
{
   private TargetContentsPresenter presenter;

   // @formatter:off
   List<TransUnit> currentPageRows = ImmutableList.<TransUnit>builder()
         .add(makeTransUnit(2))
         .add(makeTransUnit(3))
         .add(makeTransUnit(6))
         .build();
   // @formatter:on

   @Mock private EventBus eventBus;
   @Mock private TableEditorMessages tableEditorMessages;
   @Mock private SourceContentsPresenter sourceContentPresenter;
   @Mock private KeyShortcutPresenter keyShortcutPresenter;
   @Mock private NavigationMessages navMessages;
   @Mock private UserWorkspaceContext userWorkspaceContext;
   @Mock private TargetContentsDisplay display;
   @Mock
   private ToggleEditor editor, editor2;
   @Mock
   private Identity identity;
   private TransUnit selectedTU;

   // all event extends GwtEvent therefore captor will capture them all
   @Captor private ArgumentCaptor<? extends GwtEvent> eventCaptor;

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
      presenter = new TargetContentsPresenter(displayProvider, identity, eventBus, tableEditorMessages, sourceContentPresenter, sessionService, new UserConfigHolder(), userWorkspaceContext, keyShortcutPresenter, historyPresenter);

      verify(eventBus).addHandler(UserConfigChangeEvent.getType(), presenter);
      verify(eventBus).addHandler(RequestValidationEvent.getType(), presenter);
      verify(eventBus).addHandler(InsertStringInEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(CopyDataToEditorEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(TransUnitEditEvent.getType(), presenter);
      verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(), presenter);
      verify(eventBus).addHandler(ExitWorkspaceEvent.getType(), presenter);

      when(displayProvider.get()).thenReturn(display);
      presenter.showData(currentPageRows);

   }

   private void verifyRevealDisplay()
   {
      verify(keyShortcutPresenter, atLeastOnce()).setContextActive(ShortcutContext.Edit, true);
      verify(keyShortcutPresenter, atLeastOnce()).setContextActive(ShortcutContext.Navigation, false);
   }

   @Test
   public void canValidate()
   {
      when(editor.getIndex()).thenReturn(0);
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");
      when(editor.getText()).thenReturn("target");

      presenter.validate(editor);

      verify(eventBus).fireEvent(eventCaptor.capture());
      RunValidationEvent event = (RunValidationEvent) eventCaptor.getValue();
      assertThat(event.getSourceContent(), equalTo("source"));
      assertThat(event.getTarget(), equalTo("target"));
      assertThat(event.isFireNotification(), equalTo(false));
   }

   @Test
   public void canSaveAsFuzzy()
   {
      // Given: selected one trans unit with some new targets inputted
      selectedTU = currentPageRows.get(0);
      List<String> cachedTargets = Lists.newArrayList("a");
      List<String> newTargets = Lists.newArrayList("b");

      when(display.getNewTargets()).thenReturn(newTargets);
      when(display.getCachedTargets()).thenReturn(cachedTargets);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.showEditors(selectedTU.getId());

      // When:
      presenter.saveAsFuzzy(selectedTU.getId());

      // Then:
      ArgumentCaptor<TransUnitSaveEvent> captor = ArgumentCaptor.forClass(TransUnitSaveEvent.class);
      verify(eventBus, atLeastOnce()).fireEvent(captor.capture());
      TransUnitSaveEvent event = captor.getValue();

      assertThat(event.getTransUnitId(), equalTo(selectedTU.getId()));
      assertThat(event.getTargets(), Matchers.equalTo(newTargets));
      assertThat(event.getStatus(), equalTo(ContentState.NeedReview));
      assertThat(event.getOldContents(), equalTo(cachedTargets));
   }

   @Test
   public void canCopySource()
   {
      // Given: selected one trans unit
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.showEditors(selectedTU.getId());
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source");

      presenter.copySource(editor, selectedTU.getId());

      verify(editor).setTextAndValidate("source");
      verify(editor).setFocus();
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
   }


   @Test
   public void canGetNewTargets()
   {
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      List<String> targets = Lists.newArrayList("");
      when(display.getNewTargets()).thenReturn(targets);

      presenter.showEditors(selectedTU.getId());

      List<String> result = presenter.getNewTargets();

      MatcherAssert.assertThat(result, Matchers.sameInstance(targets));
   }

   @Test
   public void onRequestValidationWillNotFireRunValidationEventIfSourceAndTargetDoNotMatch()
   {
      //given current display is null
      when(sourceContentPresenter.getCurrentTransUnitIdOrNull()).thenReturn(new TransUnitId(1));

      presenter.onRequestValidation(RequestValidationEvent.EVENT);

      verifyNoMoreInteractions(eventBus);

   }

   @Test
   public void onRequestValidationWillFireRunValidationEvent()
   {
      //given current display has one editor and current editor has target content
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      when(sourceContentPresenter.getCurrentTransUnitIdOrNull()).thenReturn(selectedTU.getId());
      when(editor.getText()).thenReturn("target");
      presenter.showEditors(selectedTU.getId());

      presenter.onRequestValidation(RequestValidationEvent.EVENT);

      verify(eventBus, times(2)).fireEvent(eventCaptor.capture());//one in showEditor() one in onRequestValidation()
      RunValidationEvent event = findEvent(RunValidationEvent.class);
      MatcherAssert.assertThat(event.getTarget(), Matchers.equalTo("target"));
      verifyRevealDisplay();
   }

   private void givenCurrentEditorsAs(ToggleEditor... currentEditors)
   {
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(currentEditors));
      presenter.showEditors(selectedTU.getId());
   }

   @Test
   public void onCancelCanResetTextBack()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      ArrayList<String> targets = Lists.newArrayList("a");
      when(display.getCachedTargets()).thenReturn(targets);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.showEditors(selectedTU.getId());

      // When:
      presenter.onCancel(selectedTU.getId());

      // Then:
      verify(display, atLeastOnce()).getId();
      verify(display).getCachedTargets();
      verify(display).updateCachedAndInEditorTargets(targets);
      verify(display).highlightSearch(anyString());
   }

   @Test
   public void testOnInsertString()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      when(editor.getIndex()).thenReturn(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      when(sourceContentPresenter.getSelectedSource()).thenReturn("source content");
      presenter.showEditors(selectedTU.getId());

      // When:
      presenter.onInsertString(new InsertStringInEditorEvent("", "suggestion"));

      // Then:
      verify(editor).insertTextInCursorPosition("suggestion");

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      NotificationEvent notificationEvent = findEvent(NotificationEvent.class);
      MatcherAssert.assertThat(notificationEvent.getMessage(), Matchers.equalTo("copied"));

      RunValidationEvent runValidationEvent = findEvent(RunValidationEvent.class);
      assertThat(runValidationEvent.getSourceContent(), equalTo("source content"));
   }

   @Test
   public void testOnTransMemoryCopy()
   {
      when(tableEditorMessages.notifyCopied()).thenReturn("copied");
      givenCurrentEditorsAs(editor);

      // TODO update for plurals
      presenter.onDataCopy(new CopyDataToEditorEvent(Arrays.asList("target")));

      verify(editor).setTextAndValidate("target");
      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      NotificationEvent notificationEvent = findEvent(NotificationEvent.class);
      MatcherAssert.assertThat(notificationEvent.getMessage(), Matchers.equalTo("copied"));
   }

   @Test
   public void willMoveToNextEntryIfItIsPluralAndNotAtLastEntry()
   {
      // Given: selected display and focus on first entry
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor, editor2));
      presenter.showEditors(selectedTU.getId());

      // When:
      presenter.saveAsApprovedAndMoveNext(selectedTU.getId());

      // Then:
      verify(display).focusEditor(1);
   }

   @Test
   public void willSaveAndMoveToNextRow()
   {
      // Given: selected display and there is only one entry(no plural or last entry of plural)
      selectedTU = currentPageRows.get(0);
      List<String> cachedTargets = Lists.newArrayList("a");
      List<String> newTargets = Lists.newArrayList("b");

      when(display.getNewTargets()).thenReturn(newTargets);
      when(display.getCachedTargets()).thenReturn(cachedTargets);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.showEditors(selectedTU.getId());

      // When:
      presenter.saveAsApprovedAndMoveNext(selectedTU.getId());

      // Then:
      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());

      TransUnitSaveEvent saveEvent = findEvent(TransUnitSaveEvent.class);
      assertThat(saveEvent.getTransUnitId(), equalTo(selectedTU.getId()));
      assertThat(saveEvent.getTargets(), Matchers.equalTo(newTargets));
      assertThat(saveEvent.getStatus(), equalTo(ContentState.Approved));
      assertThat(saveEvent.getOldContents(), equalTo(cachedTargets));

      NavTransUnitEvent navEvent = findEvent(NavTransUnitEvent.class);
      assertThat(navEvent.getRowType(), equalTo(NavTransUnitEvent.NavigationType.NextEntry));
   }

   @SuppressWarnings("unchecked")
   private <E extends GwtEvent> E findEvent(final Class<E> eventType)
   {
      return (E) Iterables.find(eventCaptor.getAllValues(), new Predicate<GwtEvent>()
      {
         @Override
         public boolean apply(GwtEvent input)
         {
            return eventType.isInstance(input);
         }
      });
   }

   @Test
   public void canShowHistory()
   {
      // Given:
      selectedTU = currentPageRows.get(0);
      when(display.getId()).thenReturn(selectedTU.getId());
      when(display.getEditors()).thenReturn(Lists.newArrayList(editor));
      presenter.showEditors(selectedTU.getId());

      // When:
      presenter.showHistory(selectedTU.getId());

      // Then:
      verify(historyPresenter).showTranslationHistory(selectedTU.getId());
   }

   @Test
   public void testShowEditors()
   {

   }
}

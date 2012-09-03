package org.zanata.webtrans.client.presenter;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.PageSizeChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

@Test(groups = { "unit-tests" })
public class EditorOptionsPresenterTest
{
   private EditorOptionsPresenter presenter;
   @Mock
   private EditorOptionsDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private UserWorkspaceContext userWorkspaceContext;
   @Mock
   private ValidationOptionsPresenter validationDetailsPresenter;
   private UserConfigHolder configHolder = new UserConfigHolder();
   @Mock
   private HasValue<Boolean> needReviewChk;
   @Mock
   private HasValue<Boolean> translatedChk;
   @Mock
   private HasValue<Boolean> untranslatedChk;
   @Captor
   private ArgumentCaptor<ValueChangeHandler<Boolean>> filterChangeHandlerCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new EditorOptionsPresenter(display, eventBus, userWorkspaceContext, validationDetailsPresenter, configHolder);
      verify(display).setListener(presenter);
   }

   @Test
   public void onBindWillRegisterHandlers()
   {
      // Given: user workspace context is not readonly
      when(display.getNeedReviewChk()).thenReturn(needReviewChk);
      when(display.getTranslatedChk()).thenReturn(translatedChk);
      when(display.getUntranslatedChk()).thenReturn(untranslatedChk);
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      // When:
      presenter.onBind();

      // Then:
      verify(validationDetailsPresenter).bind();
      verify(display).getNeedReviewChk();
      verify(display).getTranslatedChk();
      verify(display).getUntranslatedChk();
      verify(needReviewChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());
      verify(translatedChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());
      verify(untranslatedChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());

      verify(eventBus).addHandler(FilterViewEvent.getType(), presenter);
      verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(), presenter);
      verify(eventBus).addHandler(EnableModalNavigationEvent.getType(), presenter);
      verify(display).setOptionsState(configHolder.getState());
   }

   @Test
   public void filterChangeHandlerWillFireEvent()
   {
      // Given: checkbox value as following
      when(display.getNeedReviewChk()).thenReturn(needReviewChk);
      when(display.getTranslatedChk()).thenReturn(translatedChk);
      when(display.getUntranslatedChk()).thenReturn(untranslatedChk);
      when(needReviewChk.getValue()).thenReturn(true);
      when(translatedChk.getValue()).thenReturn(false);
      when(untranslatedChk.getValue()).thenReturn(true);
      presenter.onBind();
      verify(needReviewChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());
      ValueChangeHandler<Boolean> handler = filterChangeHandlerCaptor.getValue();
      ArgumentCaptor<FilterViewEvent> eventCaptor = ArgumentCaptor.forClass(FilterViewEvent.class);

      // When: value change event happens
      handler.onValueChange(null);

      // Then:
      verify(eventBus).fireEvent(eventCaptor.capture());
      FilterViewEvent event = eventCaptor.getValue();
      assertThat(event.isCancelFilter(), Matchers.equalTo(false));
      assertThat(event.isFilterNeedReview(), Matchers.equalTo(true));
      assertThat(event.isFilterTranslated(), Matchers.equalTo(false));
      assertThat(event.isFilterUntranslated(), Matchers.equalTo(true));
   }

   @Test
   public void canSetReadOnlyOnWorkspaceUpdate()
   {
      // Given: project become inactive
      WorkspaceContextUpdateEvent workspaceContextUpdateEvent = new WorkspaceContextUpdateEvent(workplaceContextData(false));
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(true);

      // When:
      presenter.onWorkspaceContextUpdated(workspaceContextUpdateEvent);

      // Then:
      verify(userWorkspaceContext).setProjectActive(false);
      assertThat(configHolder.isDisplayButtons(), Matchers.is(false));
      verify(display).setOptionsState(configHolder.getState());
      verify(eventBus).fireEvent(UserConfigChangeEvent.EVENT);
   }

   private static HasWorkspaceContextUpdateData workplaceContextData(final boolean projectActive)
   {
      return new HasWorkspaceContextUpdateData()
      {
         @Override
         public boolean isProjectActive()
         {
            return projectActive;
         }
      };
   }

   @Test
   public void willToggleNavOptionOnEnableNavigationEvent()
   {
      EnableModalNavigationEvent enableEvent = new EnableModalNavigationEvent(true);
      presenter.onEnable(enableEvent);
      verify(display).setNavOptionVisible(enableEvent.isEnable());

      EnableModalNavigationEvent disableEvent = new EnableModalNavigationEvent(false);
      presenter.onEnable(disableEvent);
      verify(display).setNavOptionVisible(disableEvent.isEnable());
   }

   @Test
   public void willSetOptionsBackOnFilterViewCancelEvent()
   {
      FilterViewEvent event = new FilterViewEvent(true, true, true, true);
      when(display.getNeedReviewChk()).thenReturn(needReviewChk);
      when(display.getTranslatedChk()).thenReturn(translatedChk);
      when(display.getUntranslatedChk()).thenReturn(untranslatedChk);

      presenter.onFilterView(event);

      verify(untranslatedChk).setValue(event.isFilterUntranslated(), false);
      verify(translatedChk).setValue(event.isFilterTranslated(), false);
      verify(needReviewChk).setValue(event.isFilterNeedReview(), false);
   }

   @Test
   public void willDoNothingIfItsNotCancelEvent()
   {
      FilterViewEvent cancelEvent = new FilterViewEvent(true, true, true, false);

      presenter.onFilterView(cancelEvent);

      verifyZeroInteractions(untranslatedChk, translatedChk, needReviewChk);
   }

   @Test
   public void onPageSizeClick()
   {
      presenter.onPageSizeClick(99);

      assertThat(configHolder.getPageSize(), Matchers.equalTo(99));
      ArgumentCaptor<PageSizeChangeEvent> eventCaptor = ArgumentCaptor.forClass(PageSizeChangeEvent.class);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getPageSize(), Matchers.equalTo(99));
   }

   @Test
   public void onEnterSaveApprovedOptionChange()
   {
      configHolder.setEnterSavesApproved(false);

      presenter.onEnterSaveOptionChanged(true);

      assertThat(configHolder.isEnterSavesApproved(), Matchers.is(true));
      verify(eventBus).fireEvent(UserConfigChangeEvent.EVENT);
   }

   @Test
   public void onEscCancelEditOptionChange()
   {
      configHolder.setEscClosesEditor(false);

      presenter.onEscCancelEditOptionChanged(true);

      assertThat(configHolder.isEscClosesEditor(), Matchers.is(true));
      verify(eventBus).fireEvent(UserConfigChangeEvent.EVENT);
   }

   @Test
   public void onEditorButtonsOptionChange()
   {
      configHolder.setDisplayButtons(false);

      presenter.onEditorButtonsOptionChanged(true);

      assertThat(configHolder.isDisplayButtons(), Matchers.is(true));
      verify(eventBus).fireEvent(UserConfigChangeEvent.EVENT);
   }

   @Test
   public void onNavOptionChange()
   {
      configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);

      presenter.onSelectionChange("", NavOption.FUZZY);

      assertThat(configHolder.getNavOption(), Matchers.equalTo(NavOption.FUZZY));
      verify(eventBus).fireEvent(UserConfigChangeEvent.EVENT);
   }
}

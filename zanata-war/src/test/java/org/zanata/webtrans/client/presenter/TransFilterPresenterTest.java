package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.TransFilterDisplay;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransFilterPresenterTest
{
   private TransFilterPresenter presenter;
   @Mock
   private TransFilterDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private History history;
   @Mock
   private UserOptionsService userOptionsService;
   @Mock
   private HasValue<Boolean> needReviewChk;
   @Mock
   private HasValue<Boolean> translatedChk;
   @Mock
   private HasValue<Boolean> untranslatedChk;
   @Captor
   private ArgumentCaptor<ValueChangeHandler<Boolean>> filterChangeHandlerCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransFilterPresenter(display, eventBus, history, userOptionsService);

      when(display.getNeedReviewChk()).thenReturn(needReviewChk);
      when(display.getTranslatedChk()).thenReturn(translatedChk);
      when(display.getUntranslatedChk()).thenReturn(untranslatedChk);

      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      presenter.onBind();

      verify(eventBus).addHandler(FindMessageEvent.getType(), presenter);
      verify(eventBus).addHandler(FilterViewEvent.getType(), presenter);
      verify(display).getNeedReviewChk();
      verify(display).getTranslatedChk();
      verify(display).getUntranslatedChk();

      verify(needReviewChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());
      verify(translatedChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());
      verify(untranslatedChk).addValueChangeHandler(filterChangeHandlerCaptor.capture());
   }

   @Test
   public void testIsFocused() throws Exception
   {
      presenter.isFocused();

      verify(display).isFocused();
   }

   @Test
   public void testSearchTerm() throws Exception
   {
      HistoryToken historyToken = new HistoryToken();
      when(history.getHistoryToken()).thenReturn(historyToken);

      presenter.searchTerm("blah");

      assertThat(historyToken.getSearchText(), Matchers.equalTo("blah"));
      verify(history).newItem(historyToken);
   }

   @Test
   public void testOnFindMessage() throws Exception
   {
      presenter.onFindMessage(new FindMessageEvent("search"));

      verify(display).setSearchTerm("search");
   }

   @Test
   public void onUnbind()
   {
      presenter.onUnbind();
   }

   @Test
   public void onRevealDisplay()
   {
      presenter.onRevealDisplay();
   }

   @Test
   public void willSetOptionsBackOnFilterViewCancelEvent()
   {
      FilterViewEvent event = new FilterViewEvent(true, true, true, true);

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
   public void filterChangeHandlerWillFireEvent()
   {
      // Given: checkbox value as following
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


}

package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.TransFilterDisplay;

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

   private UserConfigHolder configHolder = new UserConfigHolder();

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);

      when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

      presenter = new TransFilterPresenter(display, eventBus, history, userOptionsService);

      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      presenter.onBind();

      verify(eventBus).addHandler(FindMessageEvent.getType(), presenter);
      verify(eventBus).addHandler(FilterViewEvent.getType(), presenter);
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
      FilterViewEvent event = new FilterViewEvent(true, true, true, false, true, null);

      presenter.onFilterView(event);

      verify(display).setTranslatedFilter(event.isFilterUntranslated());
      verify(display).setNeedReviewFilter(event.isFilterTranslated());
      verify(display).setUntranslatedFilter(event.isFilterNeedReview());
   }

   @Test
   public void willDoNothingIfItsNotCancelEvent()
   {
      FilterViewEvent cancelEvent = new FilterViewEvent(true, true, true, false, false, null);

      presenter.onFilterView(cancelEvent);

      verifyZeroInteractions(display);
   }

   @Test
   public void onUserConfigChange()
   {
      configHolder.setFilterByTranslated(true);
      configHolder.setFilterByNeedReview(false);
      configHolder.setFilterByUntranslated(true);
      configHolder.setFilterByHasError(true);

      presenter.onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

      verify(display).setTranslatedFilter(true);
      verify(display).setNeedReviewFilter(false);
      verify(display).setUntranslatedFilter(true);
      verify(display).setHasErrorFilter(true);
   }

   @Test
   public void onMessageFilterOptionChanged()
   {
      HistoryToken historyToken = new HistoryToken();
      when(history.getHistoryToken()).thenReturn(historyToken);

      presenter.messageFilterOptionChanged(true, false, true, true);

      UserConfigHolder configHolder = userOptionsService.getConfigHolder();
      assertThat(configHolder.getState().isFilterByTranslated(), Matchers.equalTo(true));
      assertThat(configHolder.getState().isFilterByNeedReview(), Matchers.equalTo(false));
      assertThat(configHolder.getState().isFilterByUntranslated(), Matchers.equalTo(true));
      assertThat(configHolder.getState().isFilterByHasError(), Matchers.equalTo(true));
      assertThat(historyToken.isFilterTranslated(), Matchers.equalTo(true));
      assertThat(historyToken.isFilterFuzzy(), Matchers.equalTo(false));
      assertThat(historyToken.isFilterUntranslated(), Matchers.equalTo(true));
      assertThat(historyToken.isFilterHasError(), Matchers.equalTo(true));
      verify(history).newItem(historyToken);
   }
}

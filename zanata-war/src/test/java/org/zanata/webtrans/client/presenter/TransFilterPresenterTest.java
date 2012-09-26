package org.zanata.webtrans.client.presenter;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.view.TransFilterDisplay;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransFilterPresenter(display, eventBus, history);
      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      presenter.onBind();

      verify(eventBus).addHandler(FindMessageEvent.getType(), presenter);
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
}

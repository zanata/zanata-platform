package org.zanata.webtrans.client.presenter;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.view.TransUnitNavigationDisplay;
import org.zanata.webtrans.shared.rpc.NavOption;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransUnitNavigationPresenterTest
{
   private TransUnitNavigationPresenter presenter;
   @Mock
   private TransUnitNavigationDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private TargetContentsPresenter targetContentsPresenter;
   @Mock
   private UserConfigHolder userConfigHolder;
   @Captor
   private ArgumentCaptor<NavTransUnitEvent> eventCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransUnitNavigationPresenter(display, eventBus, userConfigHolder, targetContentsPresenter);

      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      presenter.onBind();

      verify(eventBus).addHandler(UserConfigChangeEvent.getType(), presenter);
   }

   @Test
   public void onUserConfigChange()
   {
      when(userConfigHolder.getNavOption()).thenReturn(NavOption.UNTRANSLATED);

      presenter.onUserConfigChanged(UserConfigChangeEvent.EVENT);

      verify(display).setNavModeTooltip(NavOption.UNTRANSLATED);
   }

   @Test
   public void onGoToFirstEntry()
   {
      presenter.goToFirstEntry();

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(FirstEntry));
   }

   @Test
   public void onGoToLastEntry()
   {
      presenter.goToLastEntry();

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(LastEntry));
   }

   @Test
   public void onGoToPreviousState()
   {
      presenter.goToPreviousState();

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(PrevState));
   }

   @Test
   public void onGoToNextState()
   {
      presenter.goToNextState();

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(NextState));
   }

   @Test
   public void onGoToPreviousEntry()
   {
      presenter.goToPreviousEntry();

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(PrevEntry));
   }

   @Test
   public void onGoToNextEntry()
   {
      presenter.goToNextEntry();

      verify(targetContentsPresenter).savePendingChangesIfApplicable();
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getRowType(), Matchers.equalTo(NextEntry));
   }
}

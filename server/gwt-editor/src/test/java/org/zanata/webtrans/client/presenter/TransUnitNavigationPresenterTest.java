package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.FirstEntry;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.LastEntry;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.NextState;
import static org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType.PrevState;
import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.view.TransUnitNavigationDisplay;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitNavigationPresenterTest {
    private TransUnitNavigationPresenter presenter;
    @Mock
    private TransUnitNavigationDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private TargetContentsPresenter targetContentsPresenter;
    private UserConfigHolder userConfigHolder;
    @Captor
    private ArgumentCaptor<NavTransUnitEvent> eventCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        userConfigHolder = new UserConfigHolder();
        presenter =
                new TransUnitNavigationPresenter(display, eventBus,
                        userConfigHolder, targetContentsPresenter);

        verify(display).setListener(presenter);
    }

    @Test
    public void onBind() {
        presenter.onBind();

        verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, presenter);
    }

    @Test
    public void onUserConfigChange() {
        userConfigHolder.setNavOption(NavOption.UNTRANSLATED);

        presenter
                .onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

        verify(display).setNavModeTooltip(NavOption.UNTRANSLATED);
    }

    @Test
    public void onGoToFirstEntry() {
        presenter.goToFirstEntry();

        verify(targetContentsPresenter).savePendingChangesIfApplicable();
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getRowType()).isEqualTo(FirstEntry);
    }

    @Test
    public void onGoToLastEntry() {
        presenter.goToLastEntry();

        verify(targetContentsPresenter).savePendingChangesIfApplicable();
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getRowType()).isEqualTo(LastEntry);
    }

    @Test
    public void onGoToPreviousState() {
        presenter.goToPreviousState();

        verify(targetContentsPresenter).savePendingChangesIfApplicable();
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getRowType()).isEqualTo(PrevState);
    }

    @Test
    public void onGoToNextState() {
        presenter.goToNextState();

        verify(targetContentsPresenter).savePendingChangesIfApplicable();
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getRowType()).isEqualTo(NextState);
    }
}

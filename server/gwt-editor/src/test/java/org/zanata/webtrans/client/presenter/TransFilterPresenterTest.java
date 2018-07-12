package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.TransFilterDisplay;
import org.zanata.webtrans.shared.rpc.EditorFilter;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransFilterPresenterTest {
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
    private KeyShortcutPresenter keyShortcutPresenter;

    private UserConfigHolder configHolder = new UserConfigHolder();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        presenter =
                new TransFilterPresenter(display, eventBus, history,
                        userOptionsService, keyShortcutPresenter);

        verify(display).setListener(presenter);
    }

    @Test
    public void onBind() {
        presenter.onBind();

        verify(eventBus).addHandler(FilterViewEvent.getType(), presenter);
    }

    @Test
    public void testIsFocused() throws Exception {
        presenter.isFocused();

        verify(display).isFocused();
    }

    @Test
    public void testSearchTerm() throws Exception {
        HistoryToken historyToken = new HistoryToken();
        when(history.getHistoryToken()).thenReturn(historyToken);

        presenter.searchTerm("blah");

        assertThat(historyToken.getEditorTextSearch()).isEqualTo("blah");
        verify(history).newItem(historyToken);
    }


    @Test
    public void onUnbind() {
        presenter.onUnbind();
    }

    @Test
    public void onRevealDisplay() {
        presenter.onRevealDisplay();
    }

    @Test
    public void willSetOptionsBackOnFilterViewCancelEvent() {
        FilterViewEvent event =
            new FilterViewEvent(true, true, true, true, true, false, false,
                EditorFilter.ALL, true);
        HistoryToken historyToken = new HistoryToken();
        when(history.getHistoryToken()).thenReturn(historyToken);

        presenter.onFilterView(event);

        verify(display).setTranslatedFilter(event.isFilterUntranslated());
        verify(display).setNeedReviewFilter(event.isFilterTranslated());
        verify(display).setUntranslatedFilter(event.isFilterFuzzy());
        verify(display).setApprovedFilter(event.isFilterApproved());
        verify(display).setRejectedFilter(event.isFilterRejected());
        verify(display).setMTFilter(event.isFilterMT());

        assertThat(historyToken.isFilterUntranslated())
                .isEqualTo(event.isFilterUntranslated());
        assertThat(historyToken.isFilterTranslated())
                .isEqualTo(event.isFilterTranslated());
        assertThat(historyToken.isFilterFuzzy())
                .isEqualTo(event.isFilterFuzzy());
        assertThat(historyToken.isFilterApproved())
                .isEqualTo(event.isFilterApproved());
        assertThat(historyToken.isFilterRejected())
                .isEqualTo(event.isFilterRejected());
        assertThat(historyToken.isFilterHasError())
                .isEqualTo(event.isFilterHasError());
        assertThat(historyToken.isFilterMT())
            .isEqualTo(event.isFilterMT());
    }

    @Test
    public void willUpdateSearchTermIfItsNotCancelEvent() {
        FilterViewEvent cancelEvent =
            new FilterViewEvent(true, true, true, true, true, false, false,
                EditorFilter.ALL, false);

        presenter.onFilterView(cancelEvent);

        verify(display).setSearchTerm("");
    }

    @Test
    public void onUserConfigChange() {
        configHolder.setFilterByTranslated(true);
        configHolder.setFilterByFuzzy(false);
        configHolder.setFilterByUntranslated(true);
        configHolder.setFilterByApproved(true);
        configHolder.setFilterByRejected(true);
        configHolder.setFilterByHasError(true);
        configHolder.setFilterByMT(true);

        HistoryToken historyToken = new HistoryToken();
        when(history.getHistoryToken()).thenReturn(historyToken);

        presenter
                .onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

        verify(display).setTranslatedFilter(
                configHolder.getState().isFilterByTranslated());
        verify(display).setNeedReviewFilter(
                configHolder.getState().isFilterByFuzzy());
        verify(display).setUntranslatedFilter(
                configHolder.getState().isFilterByUntranslated());
        verify(display).setApprovedFilter(
                configHolder.getState().isFilterByApproved());
        verify(display).setRejectedFilter(
                configHolder.getState().isFilterByRejected());
        verify(display).setHasErrorFilter(
                configHolder.getState().isFilterByHasError());
        verify(display).setMTFilter(
            configHolder.getState().isFilterByMT());

        assertThat(
                historyToken.isFilterTranslated())
                .isEqualTo(configHolder.getState().isFilterByTranslated());
        assertThat(historyToken.isFilterFuzzy())
                .isEqualTo(configHolder.getState().isFilterByFuzzy());
        assertThat(historyToken.isFilterUntranslated())
                .isEqualTo(configHolder.getState()
                        .isFilterByUntranslated());
        assertThat(historyToken.isFilterApproved())
                .isEqualTo(configHolder.getState().isFilterByApproved());
        assertThat(historyToken.isFilterRejected())
                .isEqualTo(configHolder.getState().isFilterByRejected());
        assertThat(historyToken.isFilterHasError())
                .isEqualTo(configHolder.getState().isFilterByHasError());
        assertThat(historyToken.isFilterMT())
            .isEqualTo(configHolder.getState().isFilterByMT());
    }

    @Test
    public void onMessageFilterOptionChanged() {
        HistoryToken historyToken = new HistoryToken();
        when(history.getHistoryToken()).thenReturn(historyToken);

        presenter.messageFilterOptionChanged(true, false, true, true, false,
            false, false);

        UserConfigHolder configHolder = userOptionsService.getConfigHolder();
        assertThat(configHolder.getState().isFilterByTranslated()).isTrue();
        assertThat(configHolder.getState().isFilterByFuzzy()).isFalse();
        assertThat(configHolder.getState().isFilterByUntranslated()).isTrue();
        assertThat(configHolder.getState().isFilterByApproved()).isTrue();
        assertThat(configHolder.getState().isFilterByRejected()).isFalse();
        assertThat(configHolder.getState().isFilterByHasError()).isFalse();
        assertThat(historyToken.isFilterTranslated()).isTrue();
        assertThat(historyToken.isFilterFuzzy()).isFalse();
        assertThat(historyToken.isFilterUntranslated()).isTrue();
        assertThat(historyToken.isFilterApproved()).isTrue();
        assertThat(historyToken.isFilterRejected()).isFalse();
        assertThat(historyToken.isFilterHasError()).isFalse();
        assertThat(historyToken.isFilterMT()).isFalse();
        verify(history).newItem(historyToken);
    }
}

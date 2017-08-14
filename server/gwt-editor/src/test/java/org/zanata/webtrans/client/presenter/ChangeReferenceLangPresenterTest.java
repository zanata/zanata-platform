package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.ChangeReferenceLangDisplay;
import org.zanata.webtrans.shared.model.IdForLocale;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

public class ChangeReferenceLangPresenterTest {
    private ChangeReferenceLangPresenter presenter;
    @Mock
    private ChangeReferenceLangDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private ChangeReferenceLangDisplay transUnitSourceLangDisplay;
    @Mock
    private CachingDispatchAsync dispatcher;
    @Mock
    private UserOptionsService userOptionsService;
    private UserConfigHolder configHolder = new UserConfigHolder();

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        presenter = new ChangeReferenceLangPresenter(display, eventBus,
                dispatcher, userOptionsService);

        verify(display).setListener(presenter);
    }

    @Test
    public void onSourceLangListBoxOptionChangedWithValidLocale() {
        configHolder.setSelectedReferenceForSourceLang(UserConfigHolder
                .DEFAULT_SELECTED_REFERENCE);

        presenter.onSourceLangListBoxOptionChanged(new Locale(
                new IdForLocale(1L, LocaleId.EN_US), "en-US"));

        assertThat(configHolder.getState().getSelectedReferenceForSourceLang())
                .isEqualTo("en-US");
        verify(eventBus).fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onSourceLangListBoxOptionChangedWithNotChosenLocale() {
        configHolder.setSelectedReferenceForSourceLang(UserConfigHolder
                .DEFAULT_SELECTED_REFERENCE);

        presenter.onSourceLangListBoxOptionChanged(Locale.notChosenLocale);

        assertThat(configHolder.getState().getSelectedReferenceForSourceLang())
                .isEqualTo(UserConfigHolder.DEFAULT_SELECTED_REFERENCE);
        verify(eventBus).fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }
}

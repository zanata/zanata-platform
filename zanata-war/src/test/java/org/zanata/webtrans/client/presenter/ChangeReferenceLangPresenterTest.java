package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.client.view.ChangeReferenceLangDisplay;
import org.zanata.webtrans.shared.model.IdForLocale;
import org.zanata.webtrans.shared.model.Locale;

@Test(groups = {"unit-tests"})
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
    private WorkspaceId workspaceId;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        presenter = new ChangeReferenceLangPresenter(display, eventBus,
                dispatcher, userOptionsService);

        workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug",
                "iterationSlug", ProjectType.Podir), LocaleId.EN_US);

        verify(display).setListener(presenter);
    }

    @Test
    public void onSourceLangListBoxOptionChangedWithValidLocale() {
        configHolder.setSelectedReferenceForSourceLang(configHolder
                .DEFAULT_SELECTED_REFERENCE);

        presenter.onSourceLangListBoxOptionChanged(new Locale(
                new IdForLocale(1L, LocaleId.EN_US), "en-US"));

        assertThat(configHolder.getState().getSelectedReferenceForSourceLang(),
                Matchers.equalTo("en-US"));
        verify(eventBus).fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onSourceLangListBoxOptionChangedWithNotChosenLocale() {
        configHolder.setSelectedReferenceForSourceLang(configHolder
                .DEFAULT_SELECTED_REFERENCE);

        presenter.onSourceLangListBoxOptionChanged(Locale.notChosenLocale);

        assertThat(configHolder.getState().getSelectedReferenceForSourceLang(),
                Matchers.equalTo(configHolder.DEFAULT_SELECTED_REFERENCE));
        verify(eventBus).fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }
}

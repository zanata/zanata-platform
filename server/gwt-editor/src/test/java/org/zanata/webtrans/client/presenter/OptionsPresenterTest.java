package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.DocumentListOptionsDisplay;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.client.view.OptionsDisplay;
import org.zanata.webtrans.shared.rpc.ThemesOption;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

public class OptionsPresenterTest {
    private OptionsPresenter presenter;

    @Mock
    private EditorOptionsPresenter editorOptionsPresenter;
    @Mock
    private DocumentListOptionsPresenter documentListOptionsPresenter;
    @Mock
    private OptionsDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private UserOptionsService userOptionsService;

    private UserConfigHolder configHolder = new UserConfigHolder();

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        presenter =
                new OptionsPresenter(display, eventBus, editorOptionsPresenter,
                        documentListOptionsPresenter, userOptionsService);
        verify(display).setListener(presenter);
    }

    @Test
    public void onBind() {
        // When:
        presenter.onBind();

        // Then:
        verify(editorOptionsPresenter).onBind();
        verify(documentListOptionsPresenter).onBind();

        verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, presenter);
        verify(display).setShowErrorChk(configHolder.getState().isShowError());
        verify(display).setDisplayTheme(
                configHolder.getState().getDisplayTheme());
    }

    @Test
    public void onUnbind() {
        presenter.onUnbind();

        verify(editorOptionsPresenter).unbind();
        verify(documentListOptionsPresenter).unbind();
    }

    @Test
    public void setOptionsViewEditor() {
        EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
        when(editorOptionsPresenter.getDisplay()).thenReturn(widget);
        presenter.onBind();
        presenter.setOptionsView(MainView.Editor);

        verify(display).setOptions(
                editorOptionsPresenter.getDisplay().asWidget());
    }

    @Test
    public void setOptionsViewDocumentList() {
        DocumentListOptionsDisplay widget =
                mock(DocumentListOptionsDisplay.class);
        when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);
        presenter.onBind();
        presenter.setOptionsView(MainView.Documents);

        verify(display).setOptions(
                documentListOptionsPresenter.getDisplay().asWidget());
    }

    @Test
    public void setOptionsViewSearch() {
        presenter.onBind();
        presenter.setOptionsView(MainView.Search);

        verify(display).setOptions(null);
    }

    @Test
    public void onShowErrorsOptionChanged() {
        presenter.onBind();
        presenter.onShowErrorsOptionChanged(true);

        assertThat(configHolder.getState().isShowError()).isTrue();
    }

    @Test
    public void onDisplayThemeChanged() {
        presenter.onBind();
        presenter.onThemesChanged(ThemesOption.THEMES_COMPACT.name());

        assertThat(configHolder.getState().getDisplayTheme())
                .isEqualTo(ThemesOption.THEMES_COMPACT);
    }

    @Test
    public void onLoadDefaultOptionsEditor() {
        EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
        when(editorOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Editor);
        presenter.loadDefaultOptions();

        verify(editorOptionsPresenter).loadDefaultOptions();
    }

    @Test
    public void onLoadDefaultOptionsDocument() {
        DocumentListOptionsDisplay widget =
                mock(DocumentListOptionsDisplay.class);
        when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Documents);
        presenter.loadDefaultOptions();

        verify(documentListOptionsPresenter).loadDefaultOptions();
    }

    @Test
    public void persistOptionChangeEditor() {
        EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
        when(editorOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Editor);
        presenter.persistOptionChange();

        verify(editorOptionsPresenter).persistOptionChange();
    }

    @Test
    public void persistOptionChangeDocument() {
        DocumentListOptionsDisplay widget =
                mock(DocumentListOptionsDisplay.class);
        when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Documents);
        presenter.persistOptionChange();

        verify(documentListOptionsPresenter).persistOptionChange();
    }

    @Test
    public void loadOptionsEditor() {
        EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
        when(editorOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Editor);
        presenter.loadOptions();

        verify(editorOptionsPresenter).loadOptions();
    }

    @Test
    public void loadOptionsDocument() {
        DocumentListOptionsDisplay widget =
                mock(DocumentListOptionsDisplay.class);
        when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Documents);
        presenter.loadOptions();

        verify(documentListOptionsPresenter).loadOptions();
    }

    @Test
    public void loadDefaultOptionsEditor() {
        EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
        when(editorOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Editor);
        presenter.loadDefaultOptions();

        verify(editorOptionsPresenter).loadDefaultOptions();
    }

    @Test
    public void loadDefaultOptionsDocument() {
        DocumentListOptionsDisplay widget =
                mock(DocumentListOptionsDisplay.class);
        when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);

        presenter.onBind();
        presenter.setOptionsView(MainView.Documents);
        presenter.loadDefaultOptions();

        verify(documentListOptionsPresenter).loadDefaultOptions();
    }
}

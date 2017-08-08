package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.test.GWTTestData.makeTransUnit;
import static org.zanata.webtrans.test.GWTTestData.userWorkspaceContext;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.DisplaySouthPanelEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.view.TranslationEditorDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

import com.google.gwt.event.dom.client.KeyCodes;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TranslationPresenterTest {
    private TranslationPresenter presenter;
    @Mock
    private TranslationPresenter.Display display;
    @Mock
    private EventBus eventBus;
    @Mock
    private TargetContentsPresenter targetContentsPresenter;
    @Mock
    private TranslationEditorPresenter translationEditorPresenter;
    @Mock
    private TransMemoryPresenter transMemoryPresenter;
    @Mock
    private GlossaryPresenter glossaryPresenter;
    @Mock
    private WebTransMessages messages;
    private UserWorkspaceContext userWorkspaceContext;
    @Mock
    private KeyShortcutPresenter keyShortcutPresenter;
    @Mock
    private NavigationService navigationService;
    @Captor
    private ArgumentCaptor<KeyShortcut> keyShortcutCaptor;
    @Mock
    private TranslationEditorDisplay translationEditorDisplay;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        userWorkspaceContext = userWorkspaceContext();
        presenter =
                new TranslationPresenter(display, eventBus,
                        targetContentsPresenter, translationEditorPresenter,
                        transMemoryPresenter, glossaryPresenter, messages,
                        userWorkspaceContext, keyShortcutPresenter,
                        navigationService, new UserConfigHolder());
    }

    @Test
    public void onRevealDisplayIfNoActiveRow() {
        when(targetContentsPresenter.hasSelectedRow()).thenReturn(false);

        presenter.onRevealDisplay();

        verify(targetContentsPresenter).concealDisplay();
    }

    @Test
    public void onRevealDisplayIfItHasActiveRow() {
        when(targetContentsPresenter.hasSelectedRow()).thenReturn(true);

        presenter.onRevealDisplay();

        verify(targetContentsPresenter).setFocus();
        verify(targetContentsPresenter).revealDisplay();
    }

    @Test
    public void onBind() {
        TranslationPresenter spyPresenter = spy(presenter);
        doNothing().when(spyPresenter).setSouthPanelExpanded(anyBoolean());

        spyPresenter.onBind();

        verify(transMemoryPresenter).bind();
        verify(glossaryPresenter).bind();
        verify(translationEditorPresenter).bind();
        verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(),
                spyPresenter);
        verify(eventBus).addHandler(DisplaySouthPanelEvent.TYPE, spyPresenter);
        verify(spyPresenter).setSouthPanelExpanded(
                !userWorkspaceContext.hasReadOnlyAccess());
        verify(keyShortcutPresenter, times(3)).register(
                keyShortcutCaptor.capture());
    }

    @Test
    public void testKeyShortcuts() {
        when(messages.navigateToNextRow()).thenReturn("next row");
        when(messages.navigateToPreviousRow()).thenReturn("previous row");
        when(messages.openEditorInSelectedRow()).thenReturn("open editor");
        presenter.onBind();
        verify(keyShortcutPresenter, times(3)).register(
                keyShortcutCaptor.capture());
        List<KeyShortcut> shortcuts = keyShortcutCaptor.getAllValues();

        // test keys
        KeyShortcut prevKey = shortcuts.get(0);
        assertThat(prevKey.getAllKeys()).isEqualTo(Keys.setOf(new Keys(
                Keys.ALT_KEY, KeyCodes.KEY_UP), new Keys(Keys.ALT_KEY, 'J')));
        assertThat(prevKey.getDescription()).isEqualTo("previous row");
        assertThat(prevKey.getContext()).isEqualTo(ShortcutContext.Navigation);
        assertThat(prevKey.getKeyEvent()).isEqualTo(KeyShortcut.KeyEvent.KEY_DOWN);

        KeyShortcut nextKey = shortcuts.get(1);
        assertThat(nextKey.getAllKeys()).isEqualTo(Keys.setOf(new Keys(
                Keys.ALT_KEY, KeyCodes.KEY_DOWN), new Keys(Keys.ALT_KEY, 'K')));
        assertThat(nextKey.getDescription()).isEqualTo("next row");
        assertThat(nextKey.getContext()).isEqualTo(ShortcutContext.Navigation);
        assertThat(nextKey.getKeyEvent()).isEqualTo(KeyShortcut.KeyEvent.KEY_DOWN);

        KeyShortcut enterKey = shortcuts.get(2);
        assertThat(enterKey.getAllKeys()).contains(new Keys(
                Keys.NO_MODIFIER, KeyCodes.KEY_ENTER));
        assertThat(enterKey.getDescription()).isEqualTo("open editor");
        assertThat(enterKey.getContext()).isEqualTo(ShortcutContext.Navigation);
        assertThat(enterKey.getKeyEvent()).isEqualTo(KeyShortcut.KeyEvent.KEY_UP);

        // test key handlers
        prevKey.getHandler().onKeyShortcut(null);
        verify(targetContentsPresenter, atLeastOnce())
                .savePendingChangesIfApplicable();
        verify(eventBus).fireEvent(
                new NavTransUnitEvent(
                        NavTransUnitEvent.NavigationType.PrevEntry));

        nextKey.getHandler().onKeyShortcut(null);
        verify(targetContentsPresenter, atLeastOnce())
                .savePendingChangesIfApplicable();
        verify(eventBus).fireEvent(
                new NavTransUnitEvent(
                        NavTransUnitEvent.NavigationType.NextEntry));

        // by default all other presenters are not focused
        when(translationEditorPresenter.getDisplay()).thenReturn(
                translationEditorDisplay);
        enterKey.getHandler().onKeyShortcut(null);
        verify(targetContentsPresenter).setFocus();
        verify(targetContentsPresenter).revealDisplay();
    }

    @Test
    public void onUnbind() {
        presenter.onUnbind();

        verify(transMemoryPresenter).unbind();
        verify(glossaryPresenter).unbind();
        verify(translationEditorPresenter).unbind();
    }

    @Test
    public void savePendingChange() {
        presenter.saveEditorPendingChange();

        verify(targetContentsPresenter).savePendingChangesIfApplicable();
    }

    @Test
    public void onSouthPanelCollapse() {
        presenter.setSouthPanelExpanded(false);

        verify(display).setSouthPanelExpanded(false);
        verify(transMemoryPresenter).unbind();
        verify(glossaryPresenter).unbind();
    }

    @Test
    public void onSouthPanelExpandedFromCollapsed() {
        // Given: current selected trans unit and is NOT expanded
        TransUnit selection = makeTransUnit(1);
        when(navigationService.getSelectedOrNull()).thenReturn(selection);
        presenter.setSouthPanelExpanded(false);

        // When:
        presenter.setSouthPanelExpanded(true);

        // Then:
        verify(transMemoryPresenter, atLeastOnce()).bind();
        verify(glossaryPresenter, atLeastOnce()).bind();
        verify(transMemoryPresenter).createTMRequestForTransUnit(selection);
        verify(glossaryPresenter).createGlossaryRequestForTransUnit(selection);
    }

    @Test
    public void concealDisplay() {
        presenter.concealDisplay();

        verify(targetContentsPresenter).concealDisplay();
        verify(keyShortcutPresenter).setContextActive(
                ShortcutContext.Navigation, false);
    }

    @Test
    public void onWorkspaceContextUpdate() {
        WorkspaceContextUpdateEvent event =
                mock(WorkspaceContextUpdateEvent.class);
        when(event.isProjectActive()).thenReturn(false);

        presenter.onWorkspaceContextUpdated(event);
        assertThat(userWorkspaceContext.hasReadOnlyAccess()).isTrue();
        verify(transMemoryPresenter).unbind();
        verify(glossaryPresenter).unbind();
    }

    @Test
    public void onHideSouthPanel() {
        DisplaySouthPanelEvent event = mock(DisplaySouthPanelEvent.class);
        when(event.isDisplay()).thenReturn(false);
        when(transMemoryPresenter.isBound()).thenReturn(true);
        when(glossaryPresenter.isBound()).thenReturn(true);

        presenter.onDisplaySouthPanel(event);

        verify(transMemoryPresenter).unbind();
        verify(glossaryPresenter).unbind();
    }

    @Test
    public void onDisplaySouthPanel() {
        DisplaySouthPanelEvent event = mock(DisplaySouthPanelEvent.class);
        when(event.isDisplay()).thenReturn(true);

        // hide south panel first
        presenter.setSouthPanelExpanded(true);

        presenter.onDisplaySouthPanel(event);
        verify(transMemoryPresenter, atLeastOnce()).bind();
        verify(glossaryPresenter, atLeastOnce()).bind();
        verify(transMemoryPresenter, never()).unbind();
        verify(glossaryPresenter, never()).unbind();
    }

}

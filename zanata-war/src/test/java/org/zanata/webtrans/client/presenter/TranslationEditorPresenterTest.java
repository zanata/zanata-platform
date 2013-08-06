package org.zanata.webtrans.client.presenter;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.DisplaySouthPanelEvent;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.view.TransFilterDisplay;
import org.zanata.webtrans.client.view.TransUnitNavigationDisplay;
import org.zanata.webtrans.client.view.TransUnitsTableDisplay;
import org.zanata.webtrans.client.view.TranslationEditorDisplay;
import org.zanata.webtrans.client.view.TransUnitChangeSourceLangDisplay;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TranslationEditorPresenterTest
{
   private TranslationEditorPresenter presenter;
   @Mock
   private TranslationEditorDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private TransUnitNavigationPresenter transUnitNavigationPresenter;
   @Mock
   private TransFilterPresenter transFilterPresenter;
   @Mock
   private TransUnitsTablePresenter transUnitsTablePresenter;
   @Mock
   private TransFilterDisplay transFilterDisplay;
   @Mock
   private TransUnitChangeSourceLangPresenter transUnitSourceLangPresenter;
   @Mock
   private TransUnitChangeSourceLangDisplay transUnitSourceLangDisplay;
   @Mock
   private TransUnitNavigationDisplay transUnitNavigationDisplay;
   @Mock
   private TransUnitsTableDisplay transUnitsTableDisplay;
   @Mock
   private HasPager pageNavigation;
   @Mock
   private EditorKeyShortcuts editorKeyShortcuts;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TranslationEditorPresenter(display, eventBus, transUnitNavigationPresenter, transFilterPresenter, transUnitsTablePresenter, transUnitSourceLangPresenter, editorKeyShortcuts);

      when(display.getPageNavigation()).thenReturn(pageNavigation);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void onBind()
   {
      when(transFilterPresenter.getDisplay()).thenReturn(transFilterDisplay);
      when(transUnitNavigationPresenter.getDisplay()).thenReturn(transUnitNavigationDisplay);
      when(transUnitsTablePresenter.getDisplay()).thenReturn(transUnitsTableDisplay);
      when(transUnitSourceLangPresenter.getDisplay()).thenReturn(transUnitSourceLangDisplay);

      presenter.onBind();

      verify(transFilterPresenter).bind();
      verify(transFilterDisplay).asWidget();
      verify(display).setFilterView(transFilterDisplay.asWidget());

      verify(transUnitsTablePresenter).bind();
      verify(transUnitsTableDisplay).asWidget();
      verify(display).setEditorView(transUnitsTableDisplay.asWidget());

      verify(transUnitNavigationPresenter).bind();
      verify(transUnitNavigationDisplay).asWidget();
      verify(display).setTransUnitNavigation(transUnitNavigationDisplay.asWidget());

      verify(eventBus).addHandler(PageChangeEvent.TYPE, presenter);
      verify(eventBus).addHandler(PageChangeEvent.TYPE, presenter);
   }

   @Test
   public void testOnPageChange() throws Exception
   {
      PageChangeEvent event = new PageChangeEvent(2);
      presenter.onPageChange(event);

      verify(pageNavigation).setValue(event.getPageNumber());
   }

   @Test
   public void testOnPageCountChange() throws Exception
   {
      presenter.onPageCountChange(new PageCountChangeEvent(99));

      verify(pageNavigation).setPageCount(99);
   }

   @Test
   public void testOnPagerValueChanged()
   {
      presenter.onPagerValueChanged(100);
      verify(transUnitsTablePresenter).goToPage(100);
   }

   @Test
   public void testIsTransFilterFocused() throws Exception
   {
      presenter.isTransFilterFocused();

      verify(transFilterPresenter).isFocused();
   }

   @Test
   public void onUnbind()
   {
      presenter.onUnbind();

      verify(transFilterPresenter).unbind();
      verify(transUnitsTablePresenter).unbind();
      verify(transUnitNavigationPresenter).unbind();
   }

   @Test
   public void onRefreshCurrentPage()
   {
      presenter.refreshCurrentPage();

      verify(eventBus).fireEvent(RefreshPageEvent.REFRESH_CODEMIRROR_EVENT);
   }

   @Test
   public void onResizeClick()
   {
      when(display.getAndToggleResizeButton()).thenReturn(true);

      presenter.onResizeClicked();

      verify(eventBus).fireEvent(isA(DisplaySouthPanelEvent.class));
   }

   @Test
   public void onPagerFocused()
   {
      presenter.onPagerFocused();

      verify(editorKeyShortcuts).enableNavigationContext();
   }

   @Test
   public void onPagerBlurred()
   {
      presenter.onPagerBlurred();

      verify(editorKeyShortcuts).enableEditContext();
   }
}

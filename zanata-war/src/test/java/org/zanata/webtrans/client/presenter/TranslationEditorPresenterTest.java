package org.zanata.webtrans.client.presenter;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.editor.filter.TransFilterDisplay;
import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.view.TransUnitNavigationDisplay;
import org.zanata.webtrans.client.view.TransUnitsTableDisplay;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import net.customware.gwt.presenter.client.EventBus;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TranslationEditorPresenterTest
{
   private TranslationEditorPresenter presenter;
   @Mock
   private TranslationEditorPresenter.Display display;
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
   private TransUnitNavigationDisplay transUnitNavigationDisplay;
   @Mock
   private TransUnitsTableDisplay transUnitsTableDisplay;
   @Mock
   private HasPager pageNavigation;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TranslationEditorPresenter(display, eventBus, transUnitNavigationPresenter, transFilterPresenter, transUnitsTablePresenter);

      when(display.getPageNavigation()).thenReturn(pageNavigation);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void onBind()
   {
      when(transFilterPresenter.getDisplay()).thenReturn(transFilterDisplay);
      when(transUnitNavigationPresenter.getDisplay()).thenReturn(transUnitNavigationDisplay);
      when(transUnitsTablePresenter.getDisplay()).thenReturn(transUnitsTableDisplay);

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

      // test page navigation value change handler
      ArgumentCaptor<ValueChangeHandler> pageValueChangeHandlerCaptor = ArgumentCaptor.forClass(ValueChangeHandler.class);
      verify(pageNavigation).addValueChangeHandler(pageValueChangeHandlerCaptor.capture());
      ValueChangeHandler valueChangeHandler = pageValueChangeHandlerCaptor.getValue();
      valueChangeHandler.onValueChange(createMockEventWithValue(1));
      verify(transUnitsTablePresenter).goToPage(1);
   }

   private static ValueChangeEvent<Integer> createMockEventWithValue(int value)
   {
      @SuppressWarnings("unchecked")
      ValueChangeEvent<Integer> valueChangeEvent = Mockito.mock(ValueChangeEvent.class);
      when(valueChangeEvent.getValue()).thenReturn(value);
      return valueChangeEvent;
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
   public void testIsTransFilterFocused() throws Exception
   {
      presenter.isTransFilterFocused();

      verify(transFilterPresenter).isFocused();
   }

   @Test
   public void testOpenEditorOnSelectedRow() throws Exception
   {
      presenter.openEditorOnSelectedRow();

      verify(transUnitsTablePresenter).startEditing();
   }
}

package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Provider;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class SourceContentsPresenterTest
{
   private SourceContentsPresenter presenter;
   @Mock
   private EventBus eventBus;
   @Mock
   private Provider<SourceContentsDisplay> displayProvider;
   @Mock
   private SourceContentsDisplay display1;
   @Mock
   private SourceContentsDisplay display2;
   @Mock
   private HasSelectableSource hasSelectableSource;
   @Mock
   private ClickEvent clickEvent;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new SourceContentsPresenter(eventBus, displayProvider);

   }

   @Test
   public void testShowData() throws Exception
   {
      // Given: 2 trans units
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1), TestFixture.makeTransUnit(2));
      when(displayProvider.get()).thenReturn(display1, display2);

      // When:
      presenter.showData(transUnits);

      // Then:
      verify(display1).setValue(transUnits.get(0));
      verify(display1).setSourceSelectionHandler(presenter);
      verify(display2).setValue(transUnits.get(1));
      verify(display2).setSourceSelectionHandler(presenter);

      assertThat(presenter.getDisplays(), Matchers.contains(display1, display2));
   }

   @Test
   public void testSetSelectedSource() throws Exception
   {
      // Given:
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1), TestFixture.makeTransUnit(2));
      when(displayProvider.get()).thenReturn(display1, display2);
      presenter.showData(transUnits);
      when(display1.getId()).thenReturn(new TransUnitId(1));
      TransUnitId selectedId = new TransUnitId(2);
      when(display2.getId()).thenReturn(selectedId);
      when(display2.getSourcePanelList()).thenReturn(Lists.newArrayList(hasSelectableSource));

      // When: select source with id 2
      presenter.setSelectedSource(selectedId);

      // Then:
      verify(hasSelectableSource).clickSelf();
      assertThat(presenter.getCurrentTransUnitIdOrNull(), Matchers.equalTo(selectedId));
   }

   @Test
   public void testOnClick() throws Exception
   {
      // Given:
      HasSelectableSource firstSelectableSource = Mockito.mock(HasSelectableSource.class);
      when(clickEvent.getSource()).thenReturn(firstSelectableSource);

      // When: click on the first time (we don't have any previous selected source)
      presenter.onClick(clickEvent);

      // Then:
      verify(firstSelectableSource).setSelected(true);
      verify(eventBus).fireEvent(RequestValidationEvent.EVENT);

      // When: click again we will have previous selection source
      HasSelectableSource anotherSelectableSource = Mockito.mock(HasSelectableSource.class);
      when(clickEvent.getSource()).thenReturn(anotherSelectableSource);
      presenter.onClick(clickEvent);

      verify(firstSelectableSource).setSelected(false);
      verify(anotherSelectableSource).setSelected(true);
   }

   @Test
   public void testGetSelectedSource() throws Exception
   {
      String noSelectedSource = presenter.getSelectedSource();
      assertThat(noSelectedSource, Matchers.nullValue());

      when(clickEvent.getSource()).thenReturn(hasSelectableSource);
      when(hasSelectableSource.getSource()).thenReturn("source content");
      presenter.onClick(clickEvent);

      String selectedSource = presenter.getSelectedSource();
      assertThat(selectedSource, Matchers.equalTo("source content"));
   }

   @Test
   public void testHighlightSearch() throws Exception
   {
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1), TestFixture.makeTransUnit(2));
      when(displayProvider.get()).thenReturn(display1, display2);
      presenter.showData(transUnits);

      presenter.highlightSearch("search");

      verify(display1).highlightSearch("search");
      verify(display2).highlightSearch("search");
   }

   @Test
   public void withNoPriorSelectionGetCurrentTransUnitIdIsNull() throws Exception
   {
      TransUnitId transUnitId = presenter.getCurrentTransUnitIdOrNull();
      assertThat(transUnitId, Matchers.nullValue());
   }
}

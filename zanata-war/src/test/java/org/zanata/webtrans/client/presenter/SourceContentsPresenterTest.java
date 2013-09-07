package org.zanata.webtrans.client.presenter;

import java.util.List;
import javax.inject.Provider;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.GwtEvent;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import org.junit.Assert.*;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;

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
   private CachingDispatchAsync dispatcher;
   @Mock
   private Provider<SourceContentsDisplay> displayProvider;
   @Mock
   private SourceContentsDisplay display1;
   @Mock
   private SourceContentsDisplay display2;
   @Mock
   private HasSelectableSource hasSelectableSource1;
   @Mock
   private HasSelectableSource hasSelectableSource2;
   @Mock
   private ClickEvent clickEvent;
   private UserConfigHolder configHolder;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      configHolder = new UserConfigHolder();
      presenter = new SourceContentsPresenter(eventBus, displayProvider, dispatcher, configHolder);

      verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, presenter);
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
      when(display2.getSourcePanelList()).thenReturn(Lists.newArrayList(hasSelectableSource1));

      // When: select source with id 2
      presenter.setSelectedSource(selectedId);

      // Then:
      verify(hasSelectableSource1).clickSelf();
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

      when(clickEvent.getSource()).thenReturn(hasSelectableSource1);
      when(hasSelectableSource1.getSource()).thenReturn("source content");
      presenter.onClick(clickEvent);

      String selectedSource = presenter.getSelectedSource();
      assertThat(selectedSource, Matchers.equalTo("source content"));
   }

   @Test
   public void testOnClickOnDifferentRow() throws Exception
   {
      // Given:
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1), TestFixture.makeTransUnit(2));
      when(displayProvider.get()).thenReturn(display1, display2);
      presenter.showData(transUnits);
      TransUnitId previousSelectedId = transUnits.get(0).getId();
      when(display1.getId()).thenReturn(previousSelectedId);
      when(display1.getSourcePanelList()).thenReturn(Lists.newArrayList(hasSelectableSource1));
      TransUnitId selectedId = transUnits.get(1).getId();
      when(display2.getId()).thenReturn(selectedId);
      when(display2.getSourcePanelList()).thenReturn(Lists.newArrayList(hasSelectableSource1, hasSelectableSource2));

      // When: select source with id 1
      presenter.setSelectedSource(previousSelectedId);
      assertThat(presenter.getCurrentTransUnitIdOrNull(), Matchers.equalTo(previousSelectedId));

      // after select source with id 1 click on source panel in display2
      when(clickEvent.getSource()).thenReturn(hasSelectableSource2);
      when(hasSelectableSource2.getId()).thenReturn(selectedId);
      when(hasSelectableSource2.getSource()).thenReturn("source content");
      presenter.onClick(clickEvent);

      // Then:
      ArgumentCaptor<GwtEvent> eventCaptor = ArgumentCaptor.forClass(GwtEvent.class);
      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(selectedId));

      // on display2 selected it should select the second source panel
      presenter.setSelectedSource(selectedId);
      verify(hasSelectableSource2).clickSelf();
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

   @Test
   public void onTransUnitUpdated()
   {
      // Given: two display
      List<TransUnit> transUnits = Lists.newArrayList(TestFixture.makeTransUnit(1), TestFixture.makeTransUnit(2));
      when(displayProvider.get()).thenReturn(display1, display2);
      when(display1.getId()).thenReturn(transUnits.get(0).getId());
      when(display2.getId()).thenReturn(transUnits.get(1).getId());
      presenter.showData(transUnits);

      // When:
      TransUnitUpdatedEvent event = mock(TransUnitUpdatedEvent.class, Mockito.RETURNS_DEEP_STUBS);
      TransUnit updated = TestFixture.makeTransUnit(1);
      when(event.getUpdateInfo().getTransUnit()).thenReturn(updated);

      presenter.onTransUnitUpdated(event);

      verify(display1).updateTransUnitDetails(updated);
      verify(display1).refresh();
   }
}

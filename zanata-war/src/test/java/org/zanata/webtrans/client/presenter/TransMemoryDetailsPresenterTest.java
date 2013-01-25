package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Date;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.TransMemoryDetailsDisplay;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;

import com.google.common.collect.Lists;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransMemoryDetailsPresenterTest
{
   private TransMemoryDetailsPresenter presenter;
   @Mock
   private TransMemoryDetailsDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Captor
   private ArgumentCaptor<GetTransMemoryDetailsAction> actionCaptor;
   @Captor
   private ArgumentCaptor<AbstractAsyncCallback<TransMemoryDetailsList>> resultCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransMemoryDetailsPresenter(display, eventBus, dispatcher);
      verify(display).setListener(presenter);
   }

   @Test
   public void testShow() throws Exception
   {
      TransMemoryResultItem transMemoryResultItem = Mockito.mock(TransMemoryResultItem.class);
      ArrayList<Long> sourceIds = Lists.newArrayList(1L, 2L);
      when(transMemoryResultItem.getSourceIdList()).thenReturn(sourceIds);
      when(transMemoryResultItem.getSourceContents()).thenReturn(Lists.newArrayList("a", "b"));
      when(transMemoryResultItem.getTargetContents()).thenReturn(Lists.newArrayList("c", "d"));

      presenter.show(transMemoryResultItem);

      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
      assertThat(actionCaptor.getValue().getTransUnitIdList(), Matchers.equalTo(sourceIds));
      AbstractAsyncCallback<TransMemoryDetailsList> callback = resultCaptor.getValue();

      Date lastModifiedDate = new Date();
      // testing on callback success
      TransMemoryDetails details = new TransMemoryDetails("source comment", "target comment", "project", "iteration", "docId", "resId", "msgContext", "admin", lastModifiedDate);
      callback.onSuccess(new TransMemoryDetailsList(Lists.newArrayList(details)));

      InOrder inOrder = Mockito.inOrder(display);
      inOrder.verify(display).clearSourceAndTarget();
      inOrder.verify(display).setSource(transMemoryResultItem.getSourceContents());
      inOrder.verify(display).setTarget(transMemoryResultItem.getTargetContents());
      inOrder.verify(display).clearDocs();
      inOrder.verify(display).addDoc("project/iteration/docId");

      verify(display).setSourceComment("source comment");
      verify(display).setTargetComment("target comment");
      verify(display).setProjectIterationName("project / iteration");
      verify(display).setDocumentName("docId");
      verify(display).setLastModified("admin", lastModifiedDate);

      inOrder.verify(display).show();
   }

   @Test
   public void testDismissTransMemoryDetails() throws Exception
   {
      presenter.dismissTransMemoryDetails();

      verify(display).hide();
   }

   @Test
   public void testOnDocumentListBoxChanged() throws Exception
   {
      Date lastModifiedDate = new Date();
      // Given: two details
      TransMemoryDetails details1 = new TransMemoryDetails("source comment1", "target comment1", "project", "1", "docId1", "resId", "msgContext", "admin", lastModifiedDate);
      TransMemoryDetails details2 = new TransMemoryDetails("source comment2", "target comment2", "project", "2", "docId2", "resId", "msgContext", null, lastModifiedDate);
      presenter.setStatForTesting(new TransMemoryDetailsList(Lists.newArrayList(details1, details2)));

      // When: selecting second one
      when(display.getSelectedDocumentIndex()).thenReturn(1);
      presenter.onDocumentListBoxChanged();

      // Then:
      verify(display).setSourceComment("source comment2");
      verify(display).setTargetComment("target comment2");
      verify(display).setProjectIterationName("project / 2");
      verify(display).setDocumentName("docId2");
      verify(display).setLastModified(null, lastModifiedDate);
   }
}

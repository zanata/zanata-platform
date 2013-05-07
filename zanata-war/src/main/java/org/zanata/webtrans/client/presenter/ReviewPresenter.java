package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.view.ReviewDisplay;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;

import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewPresenter extends WidgetPresenter<ReviewDisplay> implements ReviewDisplay.Listener, SelectionChangeEvent.Handler
{

   private final ReviewDisplay display;
   private final EventBus eventBus;
   private final ReviewTableSelectionModel selectionModel;
   private final ReviewTableDataProvider dataProvider;
   private final CachingDispatchAsync dispatcher;
   private final GetTransUnitActionContextHolder contextHolder;

   @Inject
   public ReviewPresenter(ReviewDisplay display, EventBus eventBus, ReviewTableSelectionModel selectionModel, ReviewTableDataProvider dataProvider, CachingDispatchAsync dispatcher, GetTransUnitActionContextHolder contextHolder)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.selectionModel = selectionModel;
      this.dataProvider = dataProvider;
      this.dispatcher = dispatcher;
      this.contextHolder = contextHolder;

      init();
   }

   private void init()
   {
      display.setListener(this);
      selectionModel.addSelectionChangeHandler(this);
      display.setSelectionModel(selectionModel);
      display.setDataProvider(dataProvider);
   }

   @Override
   protected void onBind()
   {
      GetTransUnitActionContext context = contextHolder.getContext();
      GetTransUnitList getTransUnitList = GetTransUnitList.newAction(context.changeOffset(0).changeTargetTransUnitId(null).setAcceptAll());

      dataProvider.setLoading(true);
      dispatcher.execute(getTransUnitList, new AbstractAsyncCallback<GetTransUnitListResult>()
      {
         @Override
         public void onSuccess(GetTransUnitListResult result)
         {
            dataProvider.setList(result.getUnits());
         }
      });
      dataProvider.setLoading(false);
   }

   @Override
   protected void onUnbind()
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //
   }

   @Override
   protected void onRevealDisplay()
   {

   }

   @Override
   public void onSelectionChange(SelectionChangeEvent event)
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //
   }
}

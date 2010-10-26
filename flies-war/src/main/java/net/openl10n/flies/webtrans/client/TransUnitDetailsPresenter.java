package net.openl10n.flies.webtrans.client;

import net.openl10n.flies.webtrans.client.events.TransUnitSelectionEvent;
import net.openl10n.flies.webtrans.client.events.TransUnitSelectionHandler;
import net.openl10n.flies.webtrans.shared.model.TransUnit;

import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class TransUnitDetailsPresenter extends WidgetPresenter<TransUnitDetailsPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void showDetails(TransUnit transUnit);
   }

   @Inject
   public TransUnitDetailsPresenter(Display display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   @Override
   public Place getPlace()
   {
      return null;
   }

   @Override
   protected void onBind()
   {
      registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), new TransUnitSelectionHandler()
      {

         @Override
         public void onTransUnitSelected(TransUnitSelectionEvent event)
         {
            display.showDetails(event.getSelection());
         }
      }));
   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }

}

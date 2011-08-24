package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.inject.Inject;

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
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

}

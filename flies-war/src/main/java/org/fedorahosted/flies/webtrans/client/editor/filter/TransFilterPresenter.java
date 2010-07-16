package org.fedorahosted.flies.webtrans.client.editor.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.webtrans.client.editor.filter.OperatorFilter.Operator;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterPresenter.Display>
{

   public static final Place PLACE = new Place("TransUnitInfoPresenter");

   private PhraseFilter phraseFilter;

   public interface Display extends WidgetDisplay
   {
      HasValue<String> getFilterText();
   }

   @Inject
   public TransFilterPresenter(final Display display, final EventBus eventBus)
   {
      super(display, eventBus);
      this.phraseFilter = new PhraseFilter("");
   }

   @Override
   public Place getPlace()
   {
      return PLACE;
   }

   @Override
   protected void onBind()
   {

      display.getFilterText().addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            phraseFilter.setPhrase(event.getValue());
            if (!event.getValue().isEmpty())
            {
               eventBus.fireEvent(new FilterEnabledEvent(phraseFilter));
            }
            else
            {
               eventBus.fireEvent(new FilterDisabledEvent());
            }
         }
      });

   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void refreshDisplay()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void revealDisplay()
   {
      // TODO Auto-generated method stub

   }

}

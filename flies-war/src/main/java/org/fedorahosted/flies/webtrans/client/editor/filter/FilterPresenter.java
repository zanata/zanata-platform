package org.fedorahosted.flies.webtrans.client.editor.filter;

import org.fedorahosted.flies.webtrans.shared.model.TransUnit;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

public abstract class FilterPresenter<T extends ContentFilter<TransUnit>, D extends WidgetDisplay> extends WidgetPresenter<D>
{

   private T filter;

   public FilterPresenter(D display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   public void bind(T filter)
   {
      this.filter = filter;
      bind();
   }

   public T getFilter()
   {
      return filter;
   }
}

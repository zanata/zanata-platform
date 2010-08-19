package net.openl10n.flies.webtrans.client.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import net.openl10n.flies.webtrans.shared.model.TransUnit;


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

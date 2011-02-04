package net.openl10n.flies.webtrans.client.editor.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterPresenter.Display>
{
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
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

}

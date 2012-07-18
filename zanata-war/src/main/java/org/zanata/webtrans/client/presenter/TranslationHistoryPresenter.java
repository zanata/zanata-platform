package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TranslationHistoryPresenter extends WidgetPresenter<TranslationHistoryDisplay>
{
   private final TranslationHistoryDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;

   @Inject
   public TranslationHistoryPresenter(TranslationHistoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

   public void showTranslationHistory(TransUnitId transUnitId)
   {
      //TODO implement
      //throw new UnsupportedOperationException("Implement me!");
      display.center();
   }
}

package org.zanata.webtrans.client.presenter;

import java.util.List;

import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.ui.ReviewContentWrapper;
import org.zanata.webtrans.client.view.ReviewContentsDisplay;
import org.zanata.webtrans.client.view.ReviewDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.util.Finds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static com.google.common.base.Objects.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewPresenter extends WidgetPresenter<ReviewDisplay> implements ReviewDisplay.Listener, ReviewContentsDisplay.Listener
{

   private final EventBus eventBus;
   private final Provider<ReviewContentsDisplay> reviewContentsDisplayProvider;
   private final SourceContentsPresenter sourceContentsPresenter;
   private TransUnitId selectedId;
   private List<ReviewContentsDisplay> contentsDisplayList;

   @Inject
   public ReviewPresenter(ReviewDisplay display, EventBus eventBus, Provider<ReviewContentsDisplay> reviewContentsDisplayProvider, CachingDispatchAsync dispatcher, SourceContentsPresenter sourceContentsPresenter)
   {
      super(display, eventBus);
      this.eventBus = eventBus;
      this.reviewContentsDisplayProvider = reviewContentsDisplayProvider;
      this.sourceContentsPresenter = sourceContentsPresenter;

      display.setListener(this);
   }

   public void showData(List<TransUnit> transUnits)
   {
      selectedId = null; // clear cache
      ImmutableList.Builder<ReviewContentsDisplay> builder = ImmutableList.builder();
      for (TransUnit transUnit : transUnits)
      {
         ReviewContentsDisplay display = reviewContentsDisplayProvider.get();
         display.setValueAndCreateNewEditors(transUnit);
         builder.add(display);
      }
      contentsDisplayList = builder.build();
   }

   public List<ReviewContentsDisplay> getDisplays()
   {
      return contentsDisplayList;
   }

   public void setSelected(TransUnitId selectedId)
   {
      this.selectedId = selectedId;
      ReviewContentsDisplay reviewContentsDisplay = Finds.findDisplayById(contentsDisplayList, selectedId).get();

      for (ReviewContentWrapper editor : reviewContentsDisplay.getEditors())
      {
         validate(editor, reviewContentsDisplay);
      }
//      display.showButtons(isDisplayButtons());
   }

   public void validate(ReviewContentWrapper editor, ReviewContentsDisplay reviewContentsDisplay)
   {
      TransUnitId transUnitId = sourceContentsPresenter.getCurrentTransUnitIdOrNull();
      Optional<String> sourceContent = sourceContentsPresenter.getSourceContent(transUnitId);
      if (sourceContent.isPresent())
      {
         RunValidationEvent event = new RunValidationEvent(sourceContent.get(), editor.getText(), false);
         // widget that displays red outline
         event.addWidget(editor);
         // widget that displays warnings
         event.addWidget(reviewContentsDisplay);
         eventBus.fireEvent(event);
      }
   }


   public void updateRow(TransUnit updatedTransUnit)
   {
      Optional<ReviewContentsDisplay> contentsDisplayOptional = Finds.findDisplayById(contentsDisplayList, updatedTransUnit.getId());
      if (contentsDisplayOptional.isPresent())
      {
         ReviewContentsDisplay contentsDisplay = contentsDisplayOptional.get();
         contentsDisplay.setValueAndCreateNewEditors(updatedTransUnit);
         contentsDisplay.refresh();
      }
   }

   @Override
   public void acceptTranslation(TransUnitId id)
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //
   }

   @Override
   public void rejectTranslation(TransUnitId id)
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //
   }

   @Override
   protected void onBind()
   {

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
}

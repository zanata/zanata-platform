package org.zanata.webtrans.client.ui;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.ReviewCommentEvent;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;

import net.customware.gwt.presenter.client.EventBus;

public class EditorButtonsWidget extends Composite
{
   private static EditorButtonsWidgetUiBinder ourUiBinder = GWT.create(EditorButtonsWidgetUiBinder.class);
   private final EventBus eventBus;

   @UiField
   HTMLPanel buttons;
   @UiField
   InlineLabel saveIcon;
   @UiField
   InlineLabel fuzzyIcon;
   @UiField
   InlineLabel cancelIcon;
   @UiField
   InlineLabel historyIcon;
   @UiField
   SimplePanel undoContainer;
   @UiField
   Style style;
   @UiField
   InlineLabel acceptIcon;
   @UiField
   InlineLabel rejectIcon;
   @UiField
   InlineLabel commentIcon;

   private TargetContentsDisplay.Listener listener;
   private TransUnitId id;

   public EditorButtonsWidget(EventBus eventBus)
   {
      this.eventBus = eventBus;
      initWidget(ourUiBinder.createAndBindUi(this));
      displayReviewButtons(listener != null && listener.canReviewTranslation());
   }

   private void displayReviewButtons(boolean canReview)
   {
      acceptIcon.setVisible(canReview);
      rejectIcon.setVisible(canReview);
   }

   public void addUndo(final UndoLink undoLink)
   {
      undoLink.setLinkStyle("icon-undo " + style.button());
      undoLink.setUndoCallback(new UndoLink.UndoCallback()
      {
         @Override
         public void preUndo()
         {
            undoLink.setLinkStyle("icon-progress " + style.button());
         }

         @Override
         public void postUndoSuccess()
         {
            undoContainer.remove(undoLink);
         }
      });
      undoContainer.setWidget(undoLink);
   }

   @UiHandler("saveIcon")
   public void onSaveAsApproved(ClickEvent event)
   {
      listener.saveAsApprovedAndMoveNext(id);
      event.stopPropagation();
   }

   @UiHandler("fuzzyIcon")
   public void onSaveAsFuzzy(ClickEvent event)
   {
      listener.saveAsFuzzy(id);
      event.stopPropagation();
   }

   @UiHandler("cancelIcon")
   public void onCancel(ClickEvent event)
   {
      listener.onCancel(id);
      event.stopPropagation();
   }

   @UiHandler("historyIcon")
   public void onHistoryClick(ClickEvent event)
   {
      listener.showHistory(id);
      event.stopPropagation();
   }

   @UiHandler("acceptIcon")
   public void onAccept(ClickEvent event)
   {
      listener.acceptTranslation(id);
      event.stopPropagation();
   }

   @UiHandler("rejectIcon")
   public void onReject(ClickEvent event)
   {
      listener.rejectTranslation(id);
      event.stopPropagation();
   }

   @UiHandler("commentIcon")
   public void onCommentClick(ClickEvent event)
   {
      eventBus.fireEvent(new ReviewCommentEvent(id));
   }

   public void setListener(TargetContentsDisplay.Listener listener)
   {
      this.listener = listener;
      displayReviewButtons(listener.canReviewTranslation());
   }

   public void setIdAndState(TransUnitId id, ContentState state)
   {
      this.id = id;
      enableComment(state.isTranslated() || state.isRejectedOrFuzzy());
   }

   private void enableComment(boolean enable)
   {
      commentIcon.setVisible(enable);
   }


   interface EditorButtonsWidgetUiBinder extends UiBinder<HTMLPanel, EditorButtonsWidget>
   {
   }

   interface Style extends CssResource
   {

      String button();
   }
}
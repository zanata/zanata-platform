package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class ReviewModeChangeEvent extends GwtEvent<ReviewModeChangeEventHandler>
{
   public static final ReviewModeChangeEvent CHANGE_TO_REVIEW_MODE = new ReviewModeChangeEvent();
   public static final ReviewModeChangeEvent CHANGE_TO_EDIT_MODE = new ReviewModeChangeEvent();


   public static Type<ReviewModeChangeEventHandler> TYPE = new Type<ReviewModeChangeEventHandler>();

   private ReviewModeChangeEvent()
   {
   }

   public Type<ReviewModeChangeEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(ReviewModeChangeEventHandler handler)
   {
      handler.onReviewModeChange(this);
   }
}

package org.zanata.webtrans.client.events;


import java.util.List;

import javax.annotation.Nullable;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class CheckStateHasChangedEvent extends GwtEvent<CheckStateHasChangedHandler>
{
   /**
    * Handler type.
    */
   public static Type<CheckStateHasChangedHandler> TYPE = new Type<CheckStateHasChangedHandler>();
   
   private final TransUnitId transUnitId;
   private final List<String> targets;
   private final ContentState status;
   private ContentState adjustedState;


   public CheckStateHasChangedEvent(TransUnitId transUnitId, List<String> targets, ContentState status)
   {
      this.transUnitId = transUnitId;
      this.targets = targets;
      this.status = status;
      adjustedState = adjustState(targets, status);
   }


   @Override
   protected void dispatch(CheckStateHasChangedHandler handler)
   {
      handler.onCheckStateHasChanged(this);
   }

   @Override
   public GwtEvent.Type<CheckStateHasChangedHandler> getAssociatedType()
   {
      return TYPE;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public List<String> getTargets()
   {
      return targets;
   }

   public ContentState getStatus()
   {
      return status;
   }

   public ContentState getAdjustedState()
   {
      return adjustedState;
   }

   /**
    * 
    * 
    * @param newContents new target contents
    * @param requestedState requested state by user
    * @see org.zanata.service.impl.TranslationServiceImpl#adjustContentsAndState
    */
   public static ContentState adjustState(List<String> newContents, ContentState requestedState)
   {
      if (newContents == null)
      {
         return ContentState.New;
      }
      int emptyCount = Iterables.size(Iterables.filter(newContents, new Predicate<String>()
      {
         @Override
         public boolean apply(@Nullable
         String input)
         {
            return Strings.isNullOrEmpty(input);
         }
      }));

      // TODO use ContentStateUtil.determineState.
      // ContentState stateToSet =
      // ContentStateUtil.determineState(requestedState, newContents);

      // NB until then, make sure this stays consistent
      ContentState stateToSet = requestedState;
      if (requestedState == ContentState.New && emptyCount == 0)
      {
         stateToSet = ContentState.NeedReview;
      }
      else if (requestedState == ContentState.Approved && emptyCount != 0)
      {
         stateToSet = ContentState.New;
      }
      else if (requestedState == ContentState.NeedReview && emptyCount == newContents.size())
      {
         stateToSet = ContentState.New;
      }
      return stateToSet;
   }
}
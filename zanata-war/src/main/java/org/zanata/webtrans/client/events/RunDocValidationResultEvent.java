package org.zanata.webtrans.client.events;

import java.util.Date;

import com.google.gwt.event.shared.GwtEvent;

public class RunDocValidationResultEvent extends GwtEvent<RunDocValidationResultHandler>
{
   private Date startTime;
   private Date endTime;

   public RunDocValidationResultEvent(Date startTime, Date endTime)
   {
      this.startTime = startTime;
      this.endTime = endTime;
   }

   /**
    * Handler type.
    */
   private static Type<RunDocValidationResultHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<RunDocValidationResultHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<RunDocValidationResultHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<RunDocValidationResultHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(RunDocValidationResultHandler handler)
   {
      handler.onCompleteRunDocValidation(this);
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public Date getEndTime()
   {
      return endTime;
   }
}

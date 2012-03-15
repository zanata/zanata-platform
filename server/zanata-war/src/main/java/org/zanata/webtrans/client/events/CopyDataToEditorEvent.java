package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class CopyDataToEditorEvent extends GwtEvent<CopyDataToEditorHandler>
{

   /**
    * Handler type.
    */
   private static Type<CopyDataToEditorHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<CopyDataToEditorHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<CopyDataToEditorHandler>());
   }

   private String sourceResult, targetResult;

   /**
    * ContentState may be New, NeedApproved or null. stepValue may be -1 or +1.
    * 
    * @param sourceResult
    * @param targetResult
    */
   public CopyDataToEditorEvent(String sourceResult, String targetResult)
   {
      this.sourceResult = sourceResult;
      this.targetResult = targetResult;
   }

   @Override
   protected void dispatch(CopyDataToEditorHandler handler)
   {
      handler.onTransMemoryCopy(this);
   }

   @Override
   public GwtEvent.Type<CopyDataToEditorHandler> getAssociatedType()
   {
      return getType();
   }

   public String getSourceResult()
   {
      return sourceResult;
   }

   public String getTargetResult()
   {
      return targetResult;
   }
}
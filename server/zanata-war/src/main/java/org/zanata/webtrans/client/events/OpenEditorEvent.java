package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;
import org.zanata.webtrans.client.ui.ToggleEditor;

public class OpenEditorEvent extends GwtEvent<OpenEditorEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<OpenEditorEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<OpenEditorEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<OpenEditorEventHandler>();
      }
      return TYPE;
   }

   private int rowNum;
   private ToggleEditor editor;

   public OpenEditorEvent(int rowIndex, ToggleEditor editor)
   {
      this.rowNum = rowIndex;
      this.editor = editor;
   }

   public ToggleEditor getEditor()
   {
      return editor;
   }

   public int getRowNum()
   {
      return rowNum;
   }

   @Override
   protected void dispatch(OpenEditorEventHandler handler)
   {
      handler.onOpenEditor(this);
   }

   @Override
   public Type<OpenEditorEventHandler> getAssociatedType()
   {
      return getType();
   }

}
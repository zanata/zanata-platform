package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.HasTransUnitEditData;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitEditEvent extends GwtEvent<TransUnitEditEventHandler> implements HasTransUnitEditData
{

   private final EditorClientId editorClientId;
   private final Person person;
   private final TransUnit selectedTransUnit;
   private final TransUnit prevSelectedTransUnit;

   public TransUnitEditEvent(HasTransUnitEditData data)
   {
      editorClientId = data.getEditorClientId();
      person = data.getPerson();
      selectedTransUnit = data.getSelectedTransUnit();
      prevSelectedTransUnit = data.getPrevSelectedTransUnit();
   }

   /**
    * Handler type.
    */
   private static Type<TransUnitEditEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    *
    * @return returns the handler type
    */
   public static Type<TransUnitEditEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<TransUnitEditEventHandler>();
      }
      return TYPE;
   }

   @Override
   public Type<TransUnitEditEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(TransUnitEditEventHandler handler)
   {
      handler.onTransUnitEdit(this);
   }

   @Override
   public EditorClientId getEditorClientId()
   {
      return editorClientId;
   }

   @Override
   public Person getPerson()
   {
      return person;
   }

   @Override
   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   @Override
   public TransUnit getPrevSelectedTransUnit()
   {
      return prevSelectedTransUnit;
   }
}

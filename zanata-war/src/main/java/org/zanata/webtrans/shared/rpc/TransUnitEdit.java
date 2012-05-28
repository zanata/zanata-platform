package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;

public class TransUnitEdit implements SessionEventData, HasTransUnitEditData
{
   private static final long serialVersionUID = 5332535583909340461L;
   private Person person;
   private TransUnit selectedTransUnit;
   private TransUnit prevSelectedTransUnit;
   private EditorClientId editorClientId;

   public TransUnitEdit(EditorClientId editorClientId, Person person, TransUnit selectedTransUnit, TransUnit prevSelectedTransUnit)
   {
      this.editorClientId = editorClientId;
      this.person = person;
      this.selectedTransUnit = selectedTransUnit;
      this.prevSelectedTransUnit = prevSelectedTransUnit;
   }

   // for ExposeEntity
   public TransUnitEdit()
   {
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

   @Override
   public EditorClientId getEditorClientId()
   {
      return editorClientId;
   }

}
package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnitId;


public class TransUnitEditAction extends AbstractWorkspaceAction<NoOpResult>
{
   private static final long serialVersionUID = -9165857458963498055L;

   private Person person;
   private TransUnitId selectedTransUnitId;

   @SuppressWarnings("unused")
   private TransUnitEditAction()
   {
      this(null, null);
   }

   public TransUnitEditAction(Person person, TransUnitId selectedId)
   {
      this.person = person;
      this.selectedTransUnitId = selectedId;
   }

   public Person getPerson()
   {
      return person;
   }

   public TransUnitId getSelectedTransUnitId()
   {
      return selectedTransUnitId;
   }
}

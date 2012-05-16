package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;


public class TransUnitEditAction extends AbstractWorkspaceAction<TransUnitEditResult>
{
   private static final long serialVersionUID = -9165857458963498055L;

   private Person person;
   private TransUnit selectedTransUnit;
   private TransUnit prevSelectedTransUnit;


   @SuppressWarnings("unused")
   private TransUnitEditAction()
   {
      this(null, null, null);
   }

   public TransUnitEditAction(Person person, TransUnit selectedTransUnit, TransUnit prevSelectedTransUnit)
   {
      this.person = person;
      this.selectedTransUnit = selectedTransUnit;
      this.prevSelectedTransUnit = prevSelectedTransUnit;
   }

   public Person getPerson()
   {
      return person;
   }

   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   public TransUnit getPrevSelectedTransUnit()
   {
      return prevSelectedTransUnit;
   }
}

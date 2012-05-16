package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;


public class TransUnitEditAction extends AbstractWorkspaceAction<TransUnitEditResult>
{
   private static final long serialVersionUID = -9165857458963498055L;

   private Person person;
   private TransUnit selectedTransUnit;


   @SuppressWarnings("unused")
   private TransUnitEditAction()
   {
      this(null, null);
   }

   public TransUnitEditAction(Person person, TransUnit selectedTransUnit)
   {
      this.person = person;
      this.selectedTransUnit = selectedTransUnit;
   }

   public Person getPerson()
   {
      return person;
   }

   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }
}

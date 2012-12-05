package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.Person;

public class ExitWorkspaceAction extends AbstractWorkspaceAction<NoOpResult>
{

   private static final long serialVersionUID = 1L;

   private Person person;

   @SuppressWarnings("unused")
   private ExitWorkspaceAction()
   {
   }

   public ExitWorkspaceAction(Person person)
   {
      this.person = person;
   }

   public Person getPerson()
   {
      return person;
   }

}

package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;

//@ExposeEntity 
public class ExitWorkspace implements SessionEventData, HasExitWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private Person person;
   private EditorClientId editorClientId;

   // for ExposeEntity
   public ExitWorkspace()
   {
      // TODO Auto-generated constructor stub
   }

   public ExitWorkspace(EditorClientId editorClientId, Person person)
   {
      this.person = person;
      this.editorClientId = editorClientId;
   }

   @Override
   public Person getPerson()
   {
      return person;
   }

   @Override
   public EditorClientId getEditorClientId()
   {
      return editorClientId;
   }
}

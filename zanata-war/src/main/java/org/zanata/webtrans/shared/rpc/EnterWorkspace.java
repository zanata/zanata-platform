package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;

//@ExposeEntity 
public class EnterWorkspace implements SessionEventData, HasEnterWorkspaceData
{
   private static final long serialVersionUID = 1L;

   private Person person;
   private EditorClientId editorClientId;

   // for ExposeEntity
   public EnterWorkspace()
   {

   }

   public EnterWorkspace(EditorClientId editorClientId, Person person)
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

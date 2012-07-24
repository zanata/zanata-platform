package org.zanata.webtrans.shared.auth;

import org.zanata.webtrans.shared.model.Person;

import com.google.gwt.user.client.rpc.IsSerializable;


public class Identity implements IsSerializable
{
   private EditorClientId editorClientId;
   private Person person;

   @SuppressWarnings("unused")
   private Identity()
   {
   }

   public Identity(EditorClientId editorClientId, Person person)
   {
      this.editorClientId = editorClientId;
      this.person = person;
   }

   public Person getPerson()
   {
      return person;
   }

   public EditorClientId getEditorClientId()
   {
      return editorClientId;
   }

}

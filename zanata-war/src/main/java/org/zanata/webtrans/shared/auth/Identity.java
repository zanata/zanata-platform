package org.zanata.webtrans.shared.auth;

import java.io.Serializable;

import org.zanata.webtrans.shared.model.Person;


public class Identity implements Serializable
{

   private static final long serialVersionUID = 1L;

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

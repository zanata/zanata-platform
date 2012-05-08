package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = {"id", "avatarUrl"})
public class Person implements HasIdentifier<PersonId>, Serializable
{
   private static final long serialVersionUID = 510785473431813586L;

   private PersonId id;
   private String name;
   private String avatarUrl;

   public Person(PersonId id, String name, String avatarUrl)
   {
      Preconditions.checkNotNull(id, "id cannot be null");
      Preconditions.checkNotNull(name, "name cannot be null");

      this.id = id;
      this.name = name;
      this.avatarUrl = avatarUrl;
   }

   public String getAvatarUrl()
   {
      return Strings.nullToEmpty(avatarUrl);
   }
}

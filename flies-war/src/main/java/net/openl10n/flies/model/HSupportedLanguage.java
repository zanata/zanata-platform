package net.openl10n.flies.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.type.LocaleIdType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;

@Entity
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class HSupportedLanguage implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;

   @Id
   @NotNull
   @Type(type = "localeId")
   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public void setLocaleId(LocaleId localeId)
   {
      this.localeId = localeId;
   }
}

package net.openl10n.flies.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.type.LocaleIdType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;

@Entity
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class HLocale extends AbstractFliesEntity implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleId localeId;
   private boolean active;
   private Set<HPerson> members;

   @NaturalId
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

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public HLocale()
   {

   }

   public HLocale(LocaleId localeId)
   {
      this.localeId = localeId;
   }

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(name = "HLocale_Member", joinColumns = @JoinColumn(name = "supportedLanguageId"), inverseJoinColumns = @JoinColumn(name = "personId"))
   public Set<HPerson> getMembers()
   {
      return members;
   }

   public void setMembers(Set<HPerson> members)
   {
      this.members = members;
   }

   public String retrieveNativeName()
   {
      FliesLocalePair fl = new FliesLocalePair(this.localeId);
      return fl.getuLocale().getDisplayName(fl.getuLocale());
   }

   public String retrieveDisplayName()
   {
      FliesLocalePair fl = new FliesLocalePair(this.localeId);
      return fl.getuLocale().getDisplayName();
   }

}

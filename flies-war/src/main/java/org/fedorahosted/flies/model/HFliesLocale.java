package org.fedorahosted.flies.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.model.type.ULocaleType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.ibm.icu.util.ULocale;

@Entity
@TypeDef(name = "ulocale", typeClass = ULocaleType.class)
public class HFliesLocale implements Serializable
{

   private String id;
   private ULocale locale;

   private HFliesLocale parent;
   private List<HFliesLocale> children;

   private HTribe tribe;

   private List<HFliesLocale> friends; // e.g. nn, nb.

   public HFliesLocale()
   {
   }

   public HFliesLocale(ULocale locale)
   {
      setLocale(locale);
   }

   @OneToOne(mappedBy = "locale")
   public HTribe getTribe()
   {
      return tribe;
   }

   public void setTribe(HTribe tribe)
   {
      this.tribe = tribe;
   }

   public static String getFliesId(ULocale locale)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(locale.getLanguage());
      if (!locale.getCountry().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getCountry());
      }
      if (!locale.getScript().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getScript());
      }
      if (!locale.getVariant().isEmpty())
      {
         builder.append('-');
         builder.append(locale.getVariant());
      }

      return builder.toString();
   }

   @Id
   @Length(max = 80, min = 1)
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   @NotNull
   @Column(name = "icuLocaleId")
   @Type(type = "ulocale")
   public ULocale getLocale()
   {
      return locale;
   }

   public void setLocale(ULocale locale)
   {
      this.locale = locale;
      setId(getFliesId(locale));
   }

   @ManyToMany
   @JoinTable(name = "HFliesLocale_Friends", joinColumns = @JoinColumn(name = "localeId"), inverseJoinColumns = @JoinColumn(name = "friendId"))
   public List<HFliesLocale> getFriends()
   {
      return friends;
   }

   public void setFriends(List<HFliesLocale> friends)
   {
      this.friends = friends;
   }

   @OneToMany(mappedBy = "parent")
   public List<HFliesLocale> getChildren()
   {
      return children;
   }

   public void setChildren(List<HFliesLocale> children)
   {
      this.children = children;
   }

   @ManyToOne
   @JoinColumn(name = "parentId")
   public HFliesLocale getParent()
   {
      return parent;
   }

   public void setParent(HFliesLocale parent)
   {
      this.parent = parent;
   }

   private static int findLocId(ULocale fallback, ULocale[] locales)
   {
      for (int i = 0; i < locales.length; i++)
      {
         if (locales[i].equals(fallback))
            return i;
      }
      return -1;
   }

   @Transient
   public String getNativeName()
   {
      return locale.getDisplayName(locale);
   }

   @Transient
   public LocaleId getLocaleId()
   {
      return new LocaleId(id);
   }

}

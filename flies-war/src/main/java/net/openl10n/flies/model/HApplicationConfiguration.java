package net.openl10n.flies.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
public class HApplicationConfiguration extends AbstractFliesEntity
{

   public static String KEY_HOST = "flies.host";
   public static String KEY_HELP = "flies.help";
   public static String KEY_REGISTER = "flies.register";

   private String key;
   private String value;

   public HApplicationConfiguration()
   {
   }

   public HApplicationConfiguration(String key, String value)
   {
      this.key = key;
      this.value = value;
   }

   @NaturalId
   @NotEmpty
   @Length(max = 255)
   @Column(name = "config_key", nullable = false)
   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   @NotNull
   @Type(type = "text")
   @Column(name = "config_value", nullable = false)
   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }
}

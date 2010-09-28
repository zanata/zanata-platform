package net.openl10n.flies.rest.dto.extensions.gettext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import net.openl10n.flies.rest.dto.DTOUtil;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@XmlType(name = "headerEntryType")
@XmlRootElement(name = "header-entry")
@JsonPropertyOrder({ "key", "value" })
public class HeaderEntry
{
   private String key;
   private String value;

   public HeaderEntry()
   {
   }

   public HeaderEntry(String key, String value)
   {
      this.key = key;
      this.value = value;
   }

   @XmlAttribute(name = "key", required = true)
   public String getKey()
   {
      if (key == null)
         key = "";
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   @XmlValue
   public String getValue()
   {
      if (value == null)
         value = "";
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof HeaderEntry))
         return false;

      HeaderEntry other = (HeaderEntry) obj;
      return StringUtils.equals(this.key, other.key) && StringUtils.equals(this.value, other.value);
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}

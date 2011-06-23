package org.zanata.client.config;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.zanata.rest.dto.DTOUtil;


/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
@XmlType(name = "localeType")
@XmlRootElement(name = "locale")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class LocaleMapping
{

   private String locale;
   private String mapFrom;

   public LocaleMapping()
   {
      this(null, null);
   }

   /**
    * BCP47 locale ID
    * 
    * @param localeID
    */
   public LocaleMapping(String localeID)
   {
      this(localeID, null);
   }

   /**
    * 
    * 
    * @param localeID BCP47 locale ID
    * @param mapFrom locale ID used in local/client project
    */
   public LocaleMapping(String localeID, String mapFrom)
   {
      this.locale = localeID;
      this.mapFrom = mapFrom;
   }

   /**
    * BCP47 locale ID
    */
   @XmlValue
   @XmlJavaTypeAdapter(StringTrimAdapter.class)
   public String getLocale()
   {
      return locale;
   }

   public void setLocale(String localeID)
   {
      this.locale = localeID;
   }

   @XmlAttribute(name = "map-from", required = false)
   @XmlJavaTypeAdapter(StringTrimAdapter.class)
   public String getMapFrom()
   {
      return mapFrom;
   }

   public void setMapFrom(String localID)
   {
      this.mapFrom = localID;
   }

   public String getLocalLocale()
   {
      if (mapFrom != null)
         return mapFrom;
      else
         return locale;
   }

   public String getJavaLocale()
   {
      return getLocalLocale().replace('-', '_');
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((locale == null) ? 0 : locale.hashCode());
      result = prime * result + ((mapFrom == null) ? 0 : mapFrom.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof LocaleMapping))
      {
         return false;
      }
      LocaleMapping other = (LocaleMapping) obj;
      if (locale == null)
      {
         if (other.locale != null)
         {
            return false;
         }
      }
      else if (!locale.equals(other.locale))
      {
         return false;
      }
      if (mapFrom == null)
      {
         if (other.mapFrom != null)
         {
            return false;
         }
      }
      else if (!mapFrom.equals(other.mapFrom))
      {
         return false;
      }
      return true;
   }

}

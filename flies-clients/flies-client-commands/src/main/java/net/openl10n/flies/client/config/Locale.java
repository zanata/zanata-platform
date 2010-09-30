package net.openl10n.flies.client.config;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.flies.rest.dto.DTOUtil;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
@XmlType(name = "localeType")
@XmlRootElement(name = "locale")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Locale
{

   private String locale;
   private String mapFrom;

   public Locale()
   {
      this(null, null);
   }

   /**
    * BCP47 locale ID
    * 
    * @param localeID
    */
   public Locale(String localeID)
   {
      this(localeID, null);
   }

   /**
    * 
    * 
    * @param localeID BCP47 locale ID
    * @param mapFrom locale ID used in local/client project
    */
   public Locale(String localeID, String mapFrom)
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

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

}

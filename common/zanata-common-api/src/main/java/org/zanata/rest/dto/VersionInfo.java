package org.zanata.rest.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

/**
 * Holds version info
 */
@XmlRootElement(name = "versionInfo")
@XmlType(name = "versionType", propOrder = { "versionNo", "buildTimeStamp" })
@JsonTypeName(value = "versionType")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public final class VersionInfo implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String versionNo;
   private String buildTimeStamp;

   public VersionInfo(String versionNo, String buildTimestamp)
   {
      this.versionNo = versionNo;
      this.buildTimeStamp = buildTimestamp;
   }

   public VersionInfo()
   {

   }

   /**
    * @param other
    */
   public VersionInfo(VersionInfo other)
   {
      this(other.versionNo, other.buildTimeStamp);
   }

   @XmlElement(name = "versionNo")
   public String getVersionNo()
   {
      return versionNo;
   }

   @XmlElement(name = "buildTimeStamp")
   public String getBuildTimeStamp()
   {
      return buildTimeStamp;
   }

   public void setVersionNo(String versionNo)
   {
      this.versionNo = versionNo;
   }

   public void setBuildTimeStamp(String buildTimestamp)
   {
      this.buildTimeStamp = buildTimestamp;
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
      result = prime * result + ((buildTimeStamp == null) ? 0 : buildTimeStamp.hashCode());
      result = prime * result + ((versionNo == null) ? 0 : versionNo.hashCode());
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
      if (!(obj instanceof VersionInfo))
      {
         return false;
      }
      VersionInfo other = (VersionInfo) obj;
      if (buildTimeStamp == null)
      {
         if (other.buildTimeStamp != null)
         {
            return false;
         }
      }
      else if (!buildTimeStamp.equals(other.buildTimeStamp))
      {
         return false;
      }
      if (versionNo == null)
      {
         if (other.versionNo != null)
         {
            return false;
         }
      }
      else if (!versionNo.equals(other.versionNo))
      {
         return false;
      }
      return true;
   }

}

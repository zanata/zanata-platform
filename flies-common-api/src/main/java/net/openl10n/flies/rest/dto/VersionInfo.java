package net.openl10n.flies.rest.dto;

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

}

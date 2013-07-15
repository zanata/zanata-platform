package org.zanata.webtrans.shared.model;

import java.util.Date;
import java.util.List;

import org.zanata.common.ContentState;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransHistoryItem extends ComparableByDate implements IsSerializable
{
   private String versionNum;
   private List<String> contents;
   private ContentState status;
   private String modifiedBy;
   private Date modifiedDate;
   private String optionalTag = "";

   @SuppressWarnings("unused")
   private TransHistoryItem()
   {
   }

   public TransHistoryItem(String versionNum, List<String> contents, ContentState status, String modifiedBy, Date modifiedDate)
   {
      this.versionNum = versionNum;
      //targetHistory.getContents will return Hibernate persistentList which RPC can't handle
      this.contents = Lists.newArrayList(contents);
      this.status = status;
      this.modifiedBy = modifiedBy;
      this.modifiedDate = modifiedDate;
   }

   public String getVersionNum()
   {
      return versionNum;
   }

   public List<String> getContents()
   {
      return contents;
   }

   public ContentState getStatus()
   {
      return status;
   }

   public String getModifiedBy()
   {
      return modifiedBy;
   }

   public Date getModifiedDate()
   {
      return modifiedDate;
   }

   @Override
   protected Date getDate()
   {
      return modifiedDate;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("versionNum", versionNum).
            add("contents", contents).
            add("status", status).
            add("modifiedBy", modifiedBy).
            add("modifiedDate", modifiedDate).
            toString();
      // @formatter:on
   }

   public TransHistoryItem setOptionalTag(String optionalTag)
   {
      this.optionalTag = Strings.nullToEmpty(optionalTag);
      return this;
   }

   public String getOptionalTag()
   {
      return Strings.nullToEmpty(optionalTag);
   }
}

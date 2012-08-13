package org.zanata.webtrans.shared.model;

import java.util.List;

import org.zanata.common.ContentState;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransHistoryItem implements IsSerializable
{
   private String versionNum;
   private List<String> contents;
   private ContentState status;
   private String modifiedBy;
   private String modifiedDate;

   @SuppressWarnings("unused")
   private TransHistoryItem()
   {
   }

   public TransHistoryItem(String versionNum, List<String> contents, ContentState status, String modifiedBy, String modifiedDate)
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

   public String getModifiedDate()
   {
      return modifiedDate;
   }

   public void setVersionNum(String newVersionNum)
   {
      versionNum = newVersionNum;
   }
}

package org.zanata.webtrans.shared.model;

import java.util.List;

import org.zanata.common.ContentState;
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

   public TransHistoryItem(Integer versionNum, List<String> contents, ContentState status, String modifiedBy, String modifiedDate)
   {
      this.versionNum = versionNum.toString();
      this.contents = contents;
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
}

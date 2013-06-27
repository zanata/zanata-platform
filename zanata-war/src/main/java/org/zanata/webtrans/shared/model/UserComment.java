package org.zanata.webtrans.shared.model;

import java.util.List;

import org.zanata.common.ContentState;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class UserComment implements IsSerializable
{
   private String comment;
   private List<String> targetContents;
   private String madeByName;
   private String madeByDate;
   private ContentState contentState;

   @SuppressWarnings("unused")
   public UserComment()
   {
   }

   public UserComment(String comment, List<String> targetContents, String madeByName, String madeByDate, ContentState contentState)
   {
      this.comment = comment;
      this.targetContents = targetContents;
      this.madeByName = madeByName;
      this.madeByDate = madeByDate;
      this.contentState = contentState;
   }

   public String getComment()
   {
      return comment;
   }

   public List<String> getTargetContents()
   {
      return targetContents;
   }

   public String getMadeByName()
   {
      return madeByName;
   }

   public String getMadeByDate()
   {
      return madeByDate;
   }

   public ContentState getContentState()
   {
      return contentState;
   }

   // setters to make gwt serialization happy
   void setComment(String comment)
   {
      this.comment = comment;
   }

   void setTargetContents(List<String> targetContents)
   {
      this.targetContents = targetContents;
   }

   void setMadeByName(String madeByName)
   {
      this.madeByName = madeByName;
   }

   void setMadeByDate(String madeByDate)
   {
      this.madeByDate = madeByDate;
   }

   void setContentState(ContentState contentState)
   {
      this.contentState = contentState;
   }
}

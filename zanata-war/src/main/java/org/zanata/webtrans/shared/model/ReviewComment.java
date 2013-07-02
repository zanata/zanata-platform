package org.zanata.webtrans.shared.model;

import java.util.Date;
import java.util.List;

import org.zanata.common.ContentState;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewComment implements IsSerializable
{
   private ReviewCommentId id;
   private String comment;
   private List<String> targetContents;
   private String commenterName;
   private Date creationDate;
   private ContentState targetState;
   private Integer targetVersion;

   @SuppressWarnings("unused")
   public ReviewComment()
   {
   }

   public ReviewComment(ReviewCommentId id, String comment, String commenterName, Date creationDate, Integer targetVersion)
   {
      this.id = id;
      this.comment = comment;
      this.commenterName = commenterName;
      this.creationDate = creationDate;
      this.targetVersion = targetVersion;
   }

   public ReviewComment attachMetaInfo(List<String> targetContents, ContentState targetState)
   {
      this.targetContents = targetContents;
      this.targetState = targetState;
      return this;
   }

   public ReviewCommentId getId()
   {
      return id;
   }

   public String getComment()
   {
      return comment;
   }

   public List<String> getTargetContents()
   {
      return targetContents;
   }

   public String getCommenterName()
   {
      return commenterName;
   }

   public Date getCreationDate()
   {
      return creationDate;
   }

   public ContentState getTargetState()
   {
      return targetState;
   }

   public Integer getTargetVersion()
   {
      return targetVersion;
   }

   // setters to make gwt serialization happy
   void setId(ReviewCommentId id)
   {
      this.id = id;
   }

   void setComment(String comment)
   {
      this.comment = comment;
   }

   void setTargetContents(List<String> targetContents)
   {
      this.targetContents = targetContents;
   }

   void setCommenterName(String commenterName)
   {
      this.commenterName = commenterName;
   }

   void setCreationDate(Date creationDate)
   {
      this.creationDate = creationDate;
   }

   void setTargetState(ContentState targetState)
   {
      this.targetState = targetState;
   }

   void setTargetVersion(Integer targetVersion)
   {
      this.targetVersion = targetVersion;
   }
}

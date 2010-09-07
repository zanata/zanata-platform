package net.openl10n.flies.webtrans.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransMemoryDetails implements IsSerializable
{

   private String sourceComment;
   private String targetComment;
   private String projectName;
   private String iterationName;
   private String docId;

   @SuppressWarnings("unused")
   private TransMemoryDetails()
   {
      this(null, null, null, null, null);
   }

   public TransMemoryDetails(String sourceComment, String targetComment, String projectName, String iterationName, String docId)
   {
      this.sourceComment = sourceComment;
      this.targetComment = targetComment;
      this.projectName = projectName;
      this.iterationName = iterationName;
      this.docId = docId;
   }

   public String getSourceComment()
   {
      return sourceComment;
   }

   public String getTargetComment()
   {
      return targetComment;
   }

   public String getProjectName()
   {
      return projectName;
   }

   public String getIterationName()
   {
      return iterationName;
   }

   public String getDocId()
   {
      return docId;
   }
}

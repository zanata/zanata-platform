package org.zanata.webtrans.shared.model;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GlossaryDetails implements IsSerializable
{

   private String sourceTerm;
   private String targetTerm;
   private List<String> comments;

   @SuppressWarnings("unused")
   private GlossaryDetails()
   {
      this(null, null, null);
   }

   public GlossaryDetails(String sourceTerm, String targetTerm, List<String> comments)
   {
      this.sourceTerm = sourceTerm;
      this.targetTerm = targetTerm;
      this.comments = comments;
   }

   public String getSourceTerm()
   {
      return sourceTerm;
   }

   public String getTargetTerm()
   {
      return targetTerm;
   }

   public List<String> getComments()
   {
      return comments;
   }
}

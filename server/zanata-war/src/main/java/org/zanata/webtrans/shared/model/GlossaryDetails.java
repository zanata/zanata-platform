package org.zanata.webtrans.shared.model;

import java.util.List;

import org.zanata.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GlossaryDetails implements IsSerializable
{
   private List<String> sourceComment;
   private List<String> targetComment;
   private String sourceRef;
   private LocaleId srcLocale;
   private LocaleId targetLocale;

   @SuppressWarnings("unused")
   private GlossaryDetails()
   {
      this(null, null, null, null, null);
   }

   public GlossaryDetails(List<String> sourceComment, List<String> targetComment, String sourceRef, LocaleId srcLocale, LocaleId targetLocale)
   {
      this.sourceComment = sourceComment;
      this.targetComment = targetComment;
      this.sourceRef = sourceRef;
      this.srcLocale = srcLocale;
      this.targetLocale = targetLocale;
   }

   public List<String> getSourceComment()
   {
      return sourceComment;
   }

   public List<String> getTargetComment()
   {
      return targetComment;
   }

   public String getSourceRef()
   {
      return sourceRef;
   }

   public LocaleId getSrcLocale()
   {
      return srcLocale;
   }

   public LocaleId getTargetLocale()
   {
      return targetLocale;
   }
}

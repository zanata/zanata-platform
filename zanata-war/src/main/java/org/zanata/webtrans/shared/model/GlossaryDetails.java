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
   private Integer targetVersionNum;
   private String source;
   private String target;
   private String lastModifiedOn;

   @SuppressWarnings("unused")
   private GlossaryDetails()
   {
      this(null, null, null, null, null, null, null, null, null);
   }

   public GlossaryDetails(String source, String target, List<String> sourceComment, List<String> targetComment, String sourceRef, LocaleId srcLocale, LocaleId targetLocale, Integer targetVersionNum, String lastModifiedOn)
   {
      this.source = source;
      this.target = target;
      this.sourceComment = sourceComment;
      this.targetComment = targetComment;
      this.sourceRef = sourceRef;
      this.srcLocale = srcLocale;
      this.targetLocale = targetLocale;
      this.targetVersionNum = targetVersionNum;
      this.lastModifiedOn = lastModifiedOn;
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

   public Integer getTargetVersionNum()
   {
      return targetVersionNum;
   }

   public String getSource()
   {
      return source;
   }

   public String getTarget()
   {
      return target;
   }

   public String getLastModified()
   {
      return lastModifiedOn;
   }
}

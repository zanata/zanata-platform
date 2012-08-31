package org.zanata.webtrans.shared.rpc;

import org.zanata.common.LocaleId;

public class UpdateGlossaryTermAction extends AbstractWorkspaceAction<UpdateGlossaryTermResult>
{
   private static final long serialVersionUID = 1L;

   private LocaleId srcLocale, targetLocale;
   private String srcContent, targetContent;
   private Integer currentVerNum;
   
   @SuppressWarnings("unused")
   private UpdateGlossaryTermAction()
   {
      this(null, null, null, null, null);
   }
   
   public UpdateGlossaryTermAction(LocaleId srcLocale, LocaleId targetLocale, String srcContent, String targetContent, Integer currentVerNum)
   {
      this.srcLocale = srcLocale;
      this.srcContent = srcContent;
      this.targetLocale = targetLocale;
      this.targetContent = targetContent;
      this.currentVerNum = currentVerNum;
   }

   public LocaleId getSrcLocale()
   {
      return srcLocale;
   }

   public String getSrcContent()
   {
      return srcContent;
   }

   public LocaleId getTargetLocale()
   {
      return targetLocale;
   }

   public String getTargetContent()
   {
      return targetContent;
   }

   public Integer getCurrentVerNum()
   {
      return currentVerNum;
   }
}

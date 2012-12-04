package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public enum UserOptions implements IsSerializable
{
   // @formatter:off
   EnterSavesApproved(editor()+ ".EnterSavesApproved"), 
   DisplayButtons(editor()+ ".DisplayButtons"), 
   EditorPageSize(editor()+ ".PageSize"), 
   UseCodeMirrorEditor(editor()+ ".codeMirrorEditor"),
   EnableSpellCheck(editor()+ ".enableSpellCheck"),
   TransMemoryDisplayMode(editor()+ ".transMemoryDisplayMode"),
   ShowErrors(common()+ ".ShowErrors"),
   TranslatedMessageFilter(editor()+ ".TranslatedMessageFilter"), 
   NeedReviewMessageFilter(editor()+ ".NeedReviewMessageFilter"), 
   UntranslatedMessageFilter(editor()+ ".UntranslatedMessageFilter"), 
   Navigation(editor()+ ".Navigation"),
   DocumentListPageSize(doc() + ".PageSize"),
   ShowSaveApprovedWarning(editor() + ".ShowSaveApprovedWarning");
   // @formatter:on

   private String persistentName;

   public static String editor()
   {
      return "editor";
   }

   public static String doc()
   {
      return "doc";
   }

   public static String common()
   {
      return "common";
   }

   UserOptions(String persistentName)
   {
      this.persistentName = persistentName;
   }

   public String getPersistentName()
   {
      return persistentName;
   }
}

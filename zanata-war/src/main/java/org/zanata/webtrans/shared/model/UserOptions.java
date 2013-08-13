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
   DisplayTransMemory(editor()+ ".displayTransMemory"),
   DisplayGlossary(editor()+ ".displayGlossary"),
   ShowOptionalTransUnitDetails(editor()+ ".showOptionalTransUnitDetails"),
   ShowErrors(common()+ ".ShowErrors"),
   Themes(common()+ ".Themes"),
   TranslatedMessageFilter(editor()+ ".TranslatedMessageFilter"), 
   FuzzyMessageFilter(editor()+ ".FuzzyMessageFilter"), 
   UntranslatedMessageFilter(editor()+ ".UntranslatedMessageFilter"), 
   ApprovedMessageFilter(editor()+ ".ApprovedMessageFilter"), 
   RejectedMessageFilter(editor()+ ".RejectedMessageFilter"), 
   Navigation(editor()+ ".Navigation"),
   DocumentListPageSize(doc() + ".PageSize"),
   ShowSaveApprovedWarning(editor() + ".ShowSaveApprovedWarning"),
   EnableReferenceLang(editor()+ ".enableReferenceLang");
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

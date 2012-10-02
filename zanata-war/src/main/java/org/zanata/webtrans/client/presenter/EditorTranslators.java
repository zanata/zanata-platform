package org.zanata.webtrans.client.presenter;

import java.util.List;
import java.util.Map;

import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class EditorTranslators
{
   private final UserSessionService sessionService;
   private final Identity identity;

   @Inject
   public EditorTranslators(UserSessionService sessionService, Identity identity)
   {
      this.sessionService = sessionService;
      this.identity = identity;
   }

   void clearTranslatorList(List<ToggleEditor> editors)
   {
      for (ToggleEditor editor : editors)
      {
         editor.clearTranslatorList();
      }
   }

   void updateTranslator(List<ToggleEditor> editors, TransUnitId currentTransUnitId)
   {
      for (Map.Entry<EditorClientId, UserPanelSessionItem> entry : sessionService.getUserSessionMap().entrySet())
      {
         EditorClientId editorClientId = entry.getKey();
         UserPanelSessionItem panelSessionItem = entry.getValue();
         if (panelSessionItem.getSelectedTransUnit() != null)
         {
            updateEditorTranslatorList(panelSessionItem.getSelectedTransUnit().getId(), panelSessionItem.getPerson(), editorClientId, editors, currentTransUnitId);
         }
      }
   }

   private void updateEditorTranslatorList(TransUnitId selectedTransUnitId, Person person, EditorClientId editorClientId, List<ToggleEditor> editors, TransUnitId currentTransUnitId)
   {
      if (!editorClientId.equals(identity.getEditorClientId()) && Objects.equal(currentTransUnitId, selectedTransUnitId))
      {
         for (ToggleEditor editor : editors)
         {
            editor.addTranslator(person.getName(), sessionService.getColor(editorClientId));
         }
      }
      else
      {
         for (ToggleEditor editor : editors)
         {
            editor.removeTranslator(person.getName(), sessionService.getColor(editorClientId));
         }
      }
   }
}

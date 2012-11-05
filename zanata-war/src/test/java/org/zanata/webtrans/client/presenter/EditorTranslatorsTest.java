package org.zanata.webtrans.client.presenter;

import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class EditorTranslatorsTest
{
   private EditorTranslators editorTranslators;
   @Mock
   private UserSessionService sessionService;
   @Mock
   private Identity identity;
   private List<ToggleEditor> editors;
   @Mock
   private ToggleEditor editor1;
   @Mock
   private ToggleEditor editor2;
   @Mock
   private UserPanelSessionItem panelSessionItem;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      editors = Lists.newArrayList(editor1, editor2);

      editorTranslators = new EditorTranslators(sessionService, identity);
   }

   @Test
   public void testClearTranslators()
   {
      editorTranslators.clearTranslatorList(editors);

      verify(editor1).clearTranslatorList();
      verify(editor2).clearTranslatorList();
   }

   @Test
   public void testUpdateEditorTranslatorsWithConcurrentEdit()
   {
      // Given: user in session map has selected same trans unit
      EditorClientId editorClientId = new EditorClientId("sessionId", 1);
      Map<EditorClientId, UserPanelSessionItem> sessionMap = ImmutableMap.<EditorClientId, UserPanelSessionItem>builder().put(editorClientId, panelSessionItem).build();
      when(sessionService.getUserSessionMap()).thenReturn(sessionMap);
      when(sessionService.getColor(editorClientId)).thenReturn("red");
      when(panelSessionItem.getSelectedId()).thenReturn(TestFixture.makeTransUnit(1).getId());
      Person person = TestFixture.person();
      when(panelSessionItem.getPerson()).thenReturn(person);
      EditorClientId ourClientId = new EditorClientId("another client id", 2);
      when(identity.getEditorClientId()).thenReturn(ourClientId);

      // When:
      editorTranslators.updateTranslator(editors, new TransUnitId(1));

      // Then:
      verify(editor1).addTranslator("admin", "red");
      verify(editor2).addTranslator("admin", "red");
   }

   @Test
   public void testUpdateEditorTranslatorsWithoutConcurrentEdit()
   {
      // Given: user in session map has selected different trans unit
      EditorClientId editorClientId = new EditorClientId("sessionId", 1);
      Map<EditorClientId, UserPanelSessionItem> sessionMap = ImmutableMap.<EditorClientId, UserPanelSessionItem>builder().put(editorClientId, panelSessionItem).build();
      when(sessionService.getUserSessionMap()).thenReturn(sessionMap);
      when(sessionService.getColor(editorClientId)).thenReturn("red");
      when(panelSessionItem.getSelectedId()).thenReturn(TestFixture.makeTransUnit(1).getId());
      Person person = TestFixture.person();
      when(panelSessionItem.getPerson()).thenReturn(person);
      EditorClientId ourClientId = new EditorClientId("another client id", 2);
      when(identity.getEditorClientId()).thenReturn(ourClientId);

      // When:
      editorTranslators.updateTranslator(editors, new TransUnitId(2)); //different id

      // Then:
      verify(editor1).removeTranslator("admin", "red");
      verify(editor2).removeTranslator("admin", "red");
   }

   @Test
   public void sessionMapClientHasNoSelectedTransUnitYet()
   {
      // Given: user in session map don't have selected trans unit
      EditorClientId editorClientId = new EditorClientId("sessionId", 1);
      Map<EditorClientId, UserPanelSessionItem> sessionMap = ImmutableMap.<EditorClientId, UserPanelSessionItem>builder().put(editorClientId, panelSessionItem).build();
      when(sessionService.getUserSessionMap()).thenReturn(sessionMap);
      when(panelSessionItem.getSelectedId()).thenReturn(null);

      // When:
      editorTranslators.updateTranslator(editors, new TransUnitId(1));

      // Then:
      verifyZeroInteractions(editor1, editor2);
   }
}

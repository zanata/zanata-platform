/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.GlossaryDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class GlossaryPresenterTest
{
   // object under test
   private GlossaryPresenter glossaryPresenter;

   @Mock
   private GlossaryDisplay mockDisplay;
   @Mock
   private EventBus mockEventBus;
   @Mock
   private CachingDispatchAsync mockDispatcher;
   @Mock
   private WebTransMessages mockMessages;
   @Mock
   private GlossaryDetailsPresenter mockGlossaryDetailsPresenter;
   @Mock
   private UserWorkspaceContext mockUserWorkspaceContext;
   @Mock
   private KeyShortcutPresenter mockKeyShortcutPresenter;
   @Mock
   private WorkspaceContext mockWorkspaceContext;
   @Mock
   private HasText mockGlossaryTextBox;
   @Mock
   private HasValue<SearchType> mockSearchType;

   @Captor
   private ArgumentCaptor<GetGlossary> GetGlossaryCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<GetGlossaryResult>> callbackCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      glossaryPresenter = new GlossaryPresenter(mockDisplay, mockEventBus, mockDispatcher, mockMessages, mockGlossaryDetailsPresenter, mockUserWorkspaceContext, mockKeyShortcutPresenter);
   }

   @Test
   public void onBind()
   {
      when(mockMessages.searchGlossary()).thenReturn("Search glossary");

      glossaryPresenter.bind();

      verify(mockEventBus).addHandler(TransUnitSelectionEvent.getType(), glossaryPresenter);
      verify(mockKeyShortcutPresenter).register(isA(KeyShortcut.class));
      verify(mockDisplay).setListener(glossaryPresenter);
      verify(mockGlossaryDetailsPresenter).setGlossaryListener(glossaryPresenter);

   }

   @Test
   public void clearContent()
   {
      when(mockMessages.searchGlossary()).thenReturn("Search glossary");
      when(mockDisplay.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);

      glossaryPresenter.bind();
      glossaryPresenter.clearContent();

      verify(mockDisplay).clearTableContent();
      verify(mockGlossaryTextBox).setText("");
   }

   @Test
   public void onFocus()
   {
      boolean isFocused = true;

      when(mockMessages.searchGlossary()).thenReturn("Search glossary");

      glossaryPresenter.bind();
      glossaryPresenter.onFocus(isFocused);

      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Glossary, isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void onBlur()
   {
      boolean isFocused = false;

      when(mockMessages.searchGlossary()).thenReturn("Search glossary");

      glossaryPresenter.bind();
      glossaryPresenter.onFocus(isFocused);

      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Glossary, isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void showGlossaryDetail()
   {
      GlossaryResultItem object = new GlossaryResultItem("","", 0, 0);
      
      when(mockMessages.searchGlossary()).thenReturn("Search glossary");

      glossaryPresenter.bind();
      glossaryPresenter.showGlossaryDetail(object);

      verify(mockGlossaryDetailsPresenter).show(object);
   }

   @Test
   public void fireCopyEvent()
   {
      GlossaryResultItem object = new GlossaryResultItem("", "", 0, 0);
      ArgumentCaptor<InsertStringInEditorEvent> eventCaptor = ArgumentCaptor.forClass(InsertStringInEditorEvent.class);
      
      when(mockMessages.searchGlossary()).thenReturn("Search glossary");

      glossaryPresenter.bind();
      glossaryPresenter.fireCopyEvent(object);

      verify(mockEventBus).fireEvent(eventCaptor.capture());
   }

   @Test
   public void fireSearchEvent()
   {
      WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug"), LocaleId.EN_US);
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1), "test", "test/path", LocaleId.EN_US, new TranslationStats());

      when(mockMessages.searchGlossary()).thenReturn("Search glossary");
      when(mockDisplay.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);
      when(mockUserWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);
      when(mockUserWorkspaceContext.getWorkspaceContext()).thenReturn(mockWorkspaceContext);
      when(mockWorkspaceContext.getWorkspaceId()).thenReturn(workspaceId);

      glossaryPresenter.bind();
      glossaryPresenter.fireSearchEvent();

      verify(mockDisplay).startProcessing();
      verify(mockDispatcher).execute(GetGlossaryCaptor.capture(), callbackCaptor.capture());
   }
   
   @Test
   public void createGlossaryRequestForTransUnit()
   {
      WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug"), LocaleId.EN_US);
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1), "test", "test/path", LocaleId.EN_US, new TranslationStats());

      TransUnitId tuid = new TransUnitId(1);
      ArrayList<String> sources = new ArrayList<String>();
      sources.add("test source");
      ArrayList<String> targets = new ArrayList<String>();
      targets.add("test target");
      ContentState state = ContentState.values()[2];
      TransUnit.Builder builder = TransUnit.Builder.newTransUnitBuilder().setId(tuid).setResId(tuid.toString()).setLocaleId(LocaleId.EN_US).setPlural(false).setSources(sources).setSourceComment("source comment").setTargets(targets).setStatus(state).setLastModifiedBy("peter").setMsgContext("msgContext").setRowIndex(0).setVerNum(1);

      when(mockMessages.searchGlossary()).thenReturn("Search glossary");
      when(mockDisplay.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);
      when(mockUserWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);
      when(mockUserWorkspaceContext.getWorkspaceContext()).thenReturn(mockWorkspaceContext);
      when(mockWorkspaceContext.getWorkspaceId()).thenReturn(workspaceId);

      glossaryPresenter.bind();
      glossaryPresenter.createGlossaryRequestForTransUnit(builder.build());

      verify(mockDisplay).startProcessing();
      verify(mockDispatcher).execute(GetGlossaryCaptor.capture(), callbackCaptor.capture());
   }

}

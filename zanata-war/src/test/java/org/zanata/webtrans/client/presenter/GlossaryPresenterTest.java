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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.GlossaryDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyCodes;
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
   private GlossaryPresenter presenter;

   @Mock
   private GlossaryDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private WebTransMessages messages;
   @Mock
   private GlossaryDetailsPresenter glossaryDetailsPresenter;
   private UserWorkspaceContext userWorkspaceContext;
   @Mock
   private KeyShortcutPresenter keyShortcutPresenter;
   @Mock
   private HasText mockGlossaryTextBox;
   @Mock
   private HasValue<SearchType> mockSearchType;
   @Captor
   private ArgumentCaptor<GetGlossary> getGlossaryCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<GetGlossaryResult>> callbackCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      userWorkspaceContext = TestFixture.userWorkspaceContext();
      presenter = new GlossaryPresenter(display, eventBus, dispatcher, messages, glossaryDetailsPresenter, userWorkspaceContext, keyShortcutPresenter);
   }

   @Test
   public void onBind()
   {
      when(messages.searchGlossary()).thenReturn("Search glossary");

      presenter.bind();

      verify(eventBus).addHandler(TransUnitSelectionEvent.getType(), presenter);
      verify(keyShortcutPresenter).register(isA(KeyShortcut.class));
      verify(display).setListener(presenter);
      verify(glossaryDetailsPresenter).onBind();
      verify(glossaryDetailsPresenter).setGlossaryListener(presenter);

   }

   @Test
   public void clearContent()
   {
      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);

      presenter.clearContent();

      verify(display).clearTableContent();
      verify(mockGlossaryTextBox).setText("");
   }

   @Test
   public void onFocus()
   {
      boolean isFocused = true;

      when(messages.searchGlossary()).thenReturn("Search glossary");

      presenter.onFocus(isFocused);

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Glossary, isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
      assertThat(presenter.isFocused(), Matchers.is(isFocused));
   }

   @Test
   public void onBlur()
   {
      boolean isFocused = false;

      when(messages.searchGlossary()).thenReturn("Search glossary");

      presenter.onFocus(isFocused);

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Glossary, isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void showGlossaryDetail()
   {
      GlossaryResultItem object = new GlossaryResultItem("","", 0, 0);
      
      when(messages.searchGlossary()).thenReturn("Search glossary");

      presenter.showGlossaryDetail(object);

      verify(glossaryDetailsPresenter).show(object);
   }

   @Test
   public void fireCopyEvent()
   {
      GlossaryResultItem object = new GlossaryResultItem("", "", 0, 0);
      ArgumentCaptor<InsertStringInEditorEvent> eventCaptor = ArgumentCaptor.forClass(InsertStringInEditorEvent.class);
      
      when(messages.searchGlossary()).thenReturn("Search glossary");

      presenter.fireCopyEvent(object);

      verify(eventBus).fireEvent(eventCaptor.capture());
   }

   @Test
   public void fireSearchEvent()
   {
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1, ""), "test", "test/path", LocaleId.EN_US, new TranslationStats(), "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      userWorkspaceContext.setSelectedDoc(docInfo);

      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);

      presenter.fireSearchEvent();

      verify(display).startProcessing();
      verify(dispatcher).execute(getGlossaryCaptor.capture(), callbackCaptor.capture());
      GetGlossary action = getGlossaryCaptor.getValue();
      assertThat(action.getQuery(), Matchers.equalTo("query"));
      assertThat(action.getSearchType(), Matchers.equalTo(SearchType.FUZZY));
      assertThat(action.getSrcLocaleId(), Matchers.equalTo(docInfo.getSourceLocale()));
   }

   @Test
   public void fireSearchEventInSequentialWillBlockSecondRequestUntilFirstReturn()
   {
      // Given:
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1, ""), "test", "test/path", LocaleId.EN_US, new TranslationStats(), "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      userWorkspaceContext.setSelectedDoc(docInfo);
      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query1", "query2");
      when(display.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY, SearchType.FUZZY_PLURAL);

      // When: calling search glossary twice while the first one hasn't return any result
      presenter.fireSearchEvent();
      presenter.fireSearchEvent();

      // Then: there is only one RPC call to the server and the second request get ignored
      verify(display).startProcessing();
      verify(dispatcher).execute(getGlossaryCaptor.capture(), callbackCaptor.capture());
      GetGlossary action = getGlossaryCaptor.getValue();
      assertThat(action.getQuery(), Matchers.equalTo("query1"));
      assertThat(action.getSearchType(), Matchers.equalTo(SearchType.FUZZY));
      assertThat(action.getSrcLocaleId(), Matchers.equalTo(docInfo.getSourceLocale()));
   }

   @Test
   public void fireSearchEventOnSuccessCallbackWithGlossaryResults()
   {
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1, ""), "test", "test/path", LocaleId.EN_US, new TranslationStats(), "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      userWorkspaceContext.setSelectedDoc(docInfo);

      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);

      presenter.fireSearchEvent();

      verify(display).startProcessing();
      verify(dispatcher).execute(getGlossaryCaptor.capture(), callbackCaptor.capture());
      AsyncCallback<GetGlossaryResult> callback = callbackCaptor.getValue();
      ArrayList<GlossaryResultItem> glossaries = Lists.newArrayList(new GlossaryResultItem("source", "target", 100, 100));

      // on rpc callback success and result contains glossaries
      callback.onSuccess(new GetGlossaryResult(getGlossaryCaptor.getValue(), glossaries));

      verify(mockGlossaryTextBox).setText("query");
      verify(mockSearchType).setValue(SearchType.FUZZY);
      verify(display).renderTable(glossaries);
      verify(display).stopProcessing(true);
   }

   @Test
   public void fireSearchEventOnSuccessCallbackButNoGlossaryFound()
   {
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1, ""), "test", "test/path", LocaleId.EN_US, new TranslationStats(), "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      userWorkspaceContext.setSelectedDoc(docInfo);

      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);

      presenter.fireSearchEvent();

      verify(display).startProcessing();
      verify(dispatcher).execute(getGlossaryCaptor.capture(), callbackCaptor.capture());
      AsyncCallback<GetGlossaryResult> callback = callbackCaptor.getValue();

      // on rpc callback success and result contains glossaries
      callback.onSuccess(new GetGlossaryResult(getGlossaryCaptor.getValue(), Lists.<GlossaryResultItem>newArrayList()));

      verify(mockGlossaryTextBox).setText("query");
      verify(mockSearchType).setValue(SearchType.FUZZY);
      verify(display).stopProcessing(false);
   }

   @Test
   public void fireSearchEventOnFailureCallback()
   {
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1, ""), "test", "test/path", LocaleId.EN_US, new TranslationStats(), "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      userWorkspaceContext.setSelectedDoc(docInfo);

      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);

      presenter.fireSearchEvent();

      verify(display).startProcessing();
      verify(dispatcher).execute(getGlossaryCaptor.capture(), callbackCaptor.capture());
      AsyncCallback<GetGlossaryResult> callback = callbackCaptor.getValue();

      // on rpc callback failure
      callback.onFailure(new RuntimeException());

      verify(display).stopProcessing(false);
   }
   
   @Test
   public void createGlossaryRequestForTransUnit()
   {
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1, ""), "test", "test/path", LocaleId.EN_US, new TranslationStats(), "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      userWorkspaceContext.setSelectedDoc(docInfo);
      when(messages.searchGlossary()).thenReturn("Search glossary");
      when(display.getGlossaryTextBox()).thenReturn(mockGlossaryTextBox);
      when(mockGlossaryTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);
      TransUnit transUnit = TransUnit.Builder.newTransUnitBuilder().setId(1).setResId("resId").setVerNum(0)
            .setLocaleId("en").addSource("source1", "source2").setRowIndex(1).build();

      presenter.onTransUnitSelected(new TransUnitSelectionEvent(transUnit));

      verify(display).startProcessing();
      verify(dispatcher).execute(getGlossaryCaptor.capture(), callbackCaptor.capture());
      GetGlossary action = getGlossaryCaptor.getValue();
      assertThat(action.getQuery(), Matchers.equalTo("source1 source2 "));
   }

   @Test
   public void onKeyShortcut()
   {
      ArgumentCaptor<KeyShortcut> keyShortcutCaptor = ArgumentCaptor.forClass(KeyShortcut.class);
      when(messages.searchGlossary()).thenReturn("search glossary");
      GlossaryPresenter spyPresenter = spy(presenter);
      doNothing().when(spyPresenter).fireSearchEvent();

      spyPresenter.onBind();

      verify(keyShortcutPresenter).register(keyShortcutCaptor.capture());
      KeyShortcut keyShortcut = keyShortcutCaptor.getValue();
      assertThat(keyShortcut.getAllKeys(), Matchers.containsInAnyOrder(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER)));
      assertThat(keyShortcut.getContext(), Matchers.equalTo(ShortcutContext.Glossary));
      assertThat(keyShortcut.getDescription(), Matchers.equalTo("search glossary"));

      keyShortcut.getHandler().onKeyShortcut(null);
      verify(spyPresenter).fireSearchEvent();
   }

}

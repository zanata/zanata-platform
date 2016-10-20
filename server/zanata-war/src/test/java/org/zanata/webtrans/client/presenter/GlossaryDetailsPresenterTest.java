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


import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.GlossaryDetailsDisplay;
import org.zanata.webtrans.client.view.GlossaryDisplay;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class GlossaryDetailsPresenterTest {
    private GlossaryDetailsPresenter glossaryDetailsPresenter;

    @Mock
    private GlossaryDetailsDisplay display;
    @Mock
    private EventBus mockEventBus;
    @Mock
    private CachingDispatchAsync mockDispatcher;
    @Mock
    private UiMessages messages;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserWorkspaceContext mockUserWorkspaceContext;
    @Mock
    private HasText targetText;
    @Mock
    private HasText targetComment;
    @Mock
    private HasText pos;
    @Mock
    private HasText description;
    @Mock
    private HasText targetCommentText;

    @Captor
    private ArgumentCaptor<GetGlossaryDetailsAction> getGlossaryDetailsCaptor;

    @Captor
    private ArgumentCaptor<AsyncCallback<GetGlossaryDetailsResult>> getGlossarycallbackCaptor;
    @Mock
    private GlossaryDisplay.Listener glossaryListener;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        glossaryDetailsPresenter =
                new GlossaryDetailsPresenter(display, mockEventBus, messages,
                        mockDispatcher, mockUserWorkspaceContext);
        glossaryDetailsPresenter.setGlossaryListener(glossaryListener);
    }

    @Test
    public void onBind() {
        boolean hasAccess = true;

        when(
                mockUserWorkspaceContext.getWorkspaceRestrictions()
                        .isHasGlossaryUpdateAccess()).thenReturn(hasAccess);

        glossaryDetailsPresenter.bind();

        verify(display).setListener(glossaryDetailsPresenter);
    }

    @Test
    public void show() {
        GlossaryResultItem item = new GlossaryResultItem("qualifiedName", "", "", 0, 0);
        when(
                mockUserWorkspaceContext.getWorkspaceRestrictions()
                        .isHasGlossaryUpdateAccess()).thenReturn(true);

        glossaryDetailsPresenter.show(item);

        verify(mockDispatcher).execute(getGlossaryDetailsCaptor.capture(),
                getGlossarycallbackCaptor.capture());
        assertThat(getGlossaryDetailsCaptor.getValue().getSourceIdList(),
                Matchers.equalTo(item.getSourceIdList()));
        AsyncCallback<GetGlossaryDetailsResult> callback =
                getGlossarycallbackCaptor.getValue();

        // testing success callback
        GlossaryDetails glossaryDetails = mock(GlossaryDetails.class);
        when(glossaryDetails.getSource()).thenReturn("source text");
        when(glossaryDetails.getTarget()).thenReturn("target text");
        when(glossaryDetails.getSrcLocale()).thenReturn(new LocaleId("en-US"));
        when(glossaryDetails.getTargetLocale()).thenReturn(new LocaleId("zh"));
        when(glossaryDetails.getTarget()).thenReturn("source text");
        when(display.getTargetText()).thenReturn(targetText);
        when(messages.entriesLabel(1)).thenReturn("1");

        callback.onSuccess(new GetGlossaryDetailsResult(Lists
                .newArrayList(glossaryDetails)));

        verify(display).setSourceText(item.getSource());
        verify(targetText).setText(item.getSource());
        verify(display).clearEntries();
        verify(display).setSourceText(anyString());
        verify(display).setSourceLabel(anyString());
        verify(display).setTargetLabel(anyString());
        verify(display).setUrl(anyString());
        verify(display).addEntry("1");
        verify(display).center();
        verify(display).setLastModifiedDate(any());
    }
}

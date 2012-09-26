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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.GlossaryDetailsDisplay;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class GlossaryDetailsPresenterTest
{
   // object under test
   private GlossaryDetailsPresenter glossaryDetailsPresenter;

   @Mock
   private GlossaryDetailsDisplay mockDisplay;
   @Mock
   private EventBus mockEventBus;
   @Mock
   private CachingDispatchAsync mockDispatcher;
   @Mock
   private UiMessages mockMessages;
   @Mock
   private UserWorkspaceContext mockUserWorkspaceContext;
   @Mock
   private HasText mockTargetText;
   @Mock
   private HasText mockNewCommentText;

   @Captor
   private ArgumentCaptor<UpdateGlossaryTermAction> UpdateGlossaryTermCaptor;

   @Captor
   private ArgumentCaptor<GetGlossaryDetailsAction> GetGlossaryDetailsCaptor;

   @Captor
   private ArgumentCaptor<AsyncCallback<UpdateGlossaryTermResult>> updateGlossarycallbackCaptor;

   @Captor
   private ArgumentCaptor<AsyncCallback<GetGlossaryDetailsResult>> getGlossarycallbackCaptor;


   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      glossaryDetailsPresenter = new GlossaryDetailsPresenter(mockDisplay, mockEventBus, mockMessages, mockDispatcher, mockUserWorkspaceContext);
   }

   @Test
   public void onBind()
   {
      boolean hasAccess = true;

      when(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).thenReturn(hasAccess);

      glossaryDetailsPresenter.bind();
      
      verify(mockDisplay).setListener(glossaryDetailsPresenter);
      verify(mockDisplay).setHasUpdateAccess(hasAccess);
   }

   @Test
   public void onSaveClick()
   {
      String targetText = "target Text";
      String newTargetText = "new target Text";
      boolean hasAccess = true;
      
      GlossaryDetails glossaryDetails = mock(GlossaryDetails.class);

      when(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).thenReturn(hasAccess);
      when(mockDisplay.getTargetText()).thenReturn(mockTargetText);
      when(mockTargetText.getText()).thenReturn(targetText);
      when(glossaryDetails.getTarget()).thenReturn(newTargetText);

      glossaryDetailsPresenter.bind();
      glossaryDetailsPresenter.setStatesForTest(glossaryDetails);
      glossaryDetailsPresenter.onSaveClick();

      verify(mockDisplay).setHasUpdateAccess(hasAccess);
      verify(mockDisplay).showLoading(true);
      verify(mockDispatcher).execute(UpdateGlossaryTermCaptor.capture(), updateGlossarycallbackCaptor.capture());
   }

   @Test
   public void onSaveClickNoWriteAccess()
   {
      String targetText = "target Text";
      String newTargetText = "new target Text";
      boolean hasAccess = false;

      GlossaryDetails glossaryDetails = mock(GlossaryDetails.class);

      when(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).thenReturn(hasAccess);
      when(mockDisplay.getTargetText()).thenReturn(mockTargetText);
      when(mockTargetText.getText()).thenReturn(targetText);
      when(glossaryDetails.getTarget()).thenReturn(newTargetText);

      glossaryDetailsPresenter.bind();
      glossaryDetailsPresenter.setStatesForTest(glossaryDetails);
      glossaryDetailsPresenter.onSaveClick();

      verify(mockDisplay).setHasUpdateAccess(hasAccess);
   }

   @Test
   public void onDismissClick()
   {
      boolean hasAccess = true;
      GlossaryDetails glossaryDetails = mock(GlossaryDetails.class);

      when(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).thenReturn(hasAccess);

      glossaryDetailsPresenter.bind();
      glossaryDetailsPresenter.setStatesForTest(glossaryDetails);
      glossaryDetailsPresenter.onDismissClick();

      verify(mockDisplay).hide();
   }

   @Test
   public void addNewComment()
   {
      String comment = "new comment";
      int index = 0;

      boolean hasAccess = true;
      when(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).thenReturn(hasAccess);
      when(mockDisplay.getNewCommentText()).thenReturn(mockNewCommentText);
      when(mockNewCommentText.getText()).thenReturn(comment);
      
      glossaryDetailsPresenter.bind();
      glossaryDetailsPresenter.addNewComment(index);

      verify(mockDisplay).addRowIntoTargetComment(index, comment);
      verify(mockNewCommentText).setText("");
   }

   @Test
   public void show()
   {
      GlossaryResultItem item = new GlossaryResultItem("", "", 0, 0);
      
      boolean hasAccess = true;
      when(mockUserWorkspaceContext.hasGlossaryUpdateAccess()).thenReturn(hasAccess);

      glossaryDetailsPresenter.bind();
      glossaryDetailsPresenter.show(item);

      verify(mockDispatcher).execute(GetGlossaryDetailsCaptor.capture(), getGlossarycallbackCaptor.capture());
   }

}

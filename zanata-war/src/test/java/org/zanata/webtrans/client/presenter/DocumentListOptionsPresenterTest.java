package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.SaveOptionsService;
import org.zanata.webtrans.client.view.DocumentListOptionsDisplay;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

@Test(groups = { "unit-tests" })
public class DocumentListOptionsPresenterTest
{
   private DocumentListOptionsPresenter presenter;
   @Mock
   private DocumentListOptionsDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private UserWorkspaceContext userWorkspaceContext;
   private UserConfigHolder configHolder = new UserConfigHolder();
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private SaveOptionsService saveOptionsService;
   @Captor
   private ArgumentCaptor<UserConfigChangeEvent> eventCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

      presenter = new DocumentListOptionsPresenter(display, eventBus, configHolder, userWorkspaceContext, dispatcher, saveOptionsService);
     
   }

   @Test
   public void onBindWillRegisterHandlers()
   {
      // Given: user workspace context is not readonly
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      // When:
      presenter.onBind();

      // Then:
      verify(display).setListener(presenter);
      verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(), presenter);
      verify(display).setOptionsState(configHolder.getState());
   }

   @Test
   public void canSetReadOnlyOnWorkspaceUpdate()
   {
      // Given: project become inactive
      WorkspaceContextUpdateEvent workspaceContextUpdateEvent = new WorkspaceContextUpdateEvent(workplaceContextData(false));
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(true);

      // When:
      presenter.onBind();
      presenter.onWorkspaceContextUpdated(workspaceContextUpdateEvent);

      // Then:
      assertThat(configHolder.getDocumentListPageSize(), Matchers.equalTo(25));
      assertThat(configHolder.isShowError(), Matchers.equalTo(false));
      
      verify(userWorkspaceContext).setProjectActive(false);
      verify(display).setOptionsState(configHolder.getState());

      verify(eventBus, times(2)).fireEvent(isA(UserConfigChangeEvent.class));
      verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
   }

   private static HasWorkspaceContextUpdateData workplaceContextData(final boolean projectActive)
   {
      return new HasWorkspaceContextUpdateData()
      {
         @Override
         public boolean isProjectActive()
         {
            return projectActive;
         }
      };
   }

   @Test
   public void onPageSizeClick()
   {
      presenter.onBind();
      presenter.onPageSizeClick(99);

      assertThat(configHolder.getDocumentListPageSize(), Matchers.equalTo(99));
      ArgumentCaptor<UserConfigChangeEvent> eventCaptor = ArgumentCaptor.forClass(UserConfigChangeEvent.class);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getView(), Matchers.equalTo(MainView.Documents));
   }

   @Test
   public void onLoadDefaultOptions()
   {
      presenter.loadDefaultOptions();

      assertThat(configHolder.getDocumentListPageSize(), Matchers.equalTo(25));
      assertThat(configHolder.isShowError(), Matchers.equalTo(false));
      verify(display).setOptionsState(configHolder.getState());
      verify(eventBus).fireEvent(isA(UserConfigChangeEvent.class));
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
   }
   
   @Test
   public void onPersistOption()
   {
      configHolder.setDocumentListPageSize(50);
      configHolder.setShowError(true);

      presenter.persistOptionChange();

      ArgumentCaptor<SaveOptionsAction> actionCaptor = ArgumentCaptor.forClass(SaveOptionsAction.class);
      ArgumentCaptor<AsyncCallback> callbackCaptor = ArgumentCaptor.forClass(AsyncCallback.class);
      verify(dispatcher).execute(actionCaptor.capture(), callbackCaptor.capture());

      SaveOptionsAction action = actionCaptor.getValue();
      int docListSize = Integer.parseInt(action.getConfigurationMap().get(UserOptions.DocumentListPageSize));
      boolean showError = Boolean.parseBoolean(action.getConfigurationMap().get(UserOptions.ShowErrors));
      
      assertThat(docListSize, Matchers.equalTo(50));
      assertThat(showError, Matchers.equalTo(true));

      AsyncCallback<SaveOptionsResult> callback = callbackCaptor.getValue();
      callback.onSuccess(new SaveOptionsResult());
      callback.onFailure(null);
      verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
   }
   
   @Test
   public void onLoadSavedOption()
   {
      UserConfigHolder configHolder = new UserConfigHolder();
      configHolder.setDocumentListPageSize(100);
      configHolder.setShowError(true);

      LoadOptionsResult result = new LoadOptionsResult();
      result.setConfiguration( configHolder.getState() );

      presenter.loadOptions();

      ArgumentCaptor<LoadOptionsAction> actionCaptor = ArgumentCaptor.forClass(LoadOptionsAction.class);
      ArgumentCaptor<AsyncCallback> callbackCaptor = ArgumentCaptor.forClass(AsyncCallback.class);
      verify(dispatcher).execute(actionCaptor.capture(), callbackCaptor.capture());

      AsyncCallback callback = callbackCaptor.getValue();

      callback.onSuccess(result);
      assertThat(configHolder.getDocumentListPageSize(), Matchers.equalTo(100));
      assertThat(configHolder.isShowError(), Matchers.equalTo(true));

      verify(eventBus).fireEvent(isA(UserConfigChangeEvent.class));

      callback.onFailure(null);
      verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
   }
}

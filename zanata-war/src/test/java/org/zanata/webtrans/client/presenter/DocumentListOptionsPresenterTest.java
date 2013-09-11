package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.DocumentListOptionsDisplay;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;

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
   @Mock
   private WorkspaceContext workspaceContext;
   private UserConfigHolder configHolder;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private UserOptionsService userOptionsService;
   @Captor
   private ArgumentCaptor<UserConfigChangeEvent> eventCaptor;

   private WorkspaceId workspaceId;

   @BeforeMethod
   public void beforeMethod()
   {
      configHolder = new UserConfigHolder();
      MockitoAnnotations.initMocks(this);

      presenter = new DocumentListOptionsPresenter(display, eventBus, userWorkspaceContext, dispatcher, userOptionsService);
      when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

      workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug", ProjectType.Podir), LocaleId.EN_US);

      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(workspaceContext);
      when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);
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
      verify(display).setOptionsState(userOptionsService.getConfigHolder().getState());
   }

   @Test
   public void canSetReadOnlyOnWorkspaceUpdate()
   {
      // Given: project become inactive
      WorkspaceContextUpdateEvent workspaceContextUpdateEvent = new WorkspaceContextUpdateEvent(workplaceContextData(false, ProjectType.Podir));
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(true);

      // When:
      presenter.onBind();
      presenter.onWorkspaceContextUpdated(workspaceContextUpdateEvent);

      // Then:
      assertThat(configHolder.getState().getDocumentListPageSize(), Matchers.equalTo(25));
      assertThat(configHolder.getState().isShowError(), Matchers.equalTo(false));
      
      verify(userWorkspaceContext).setProjectActive(false);
      verify(display, times(3)).setOptionsState(isA(UserConfigHolder.ConfigurationState.class));

      verify(eventBus, times(2)).fireEvent(isA(UserConfigChangeEvent.class));
      verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
   }

   private static HasWorkspaceContextUpdateData workplaceContextData(final boolean projectActive, final ProjectType projectType)
   {
      return new HasWorkspaceContextUpdateData()
      {
         @Override
         public boolean isProjectActive()
         {
            return projectActive;
         }

         @Override
         public ProjectType getProjectType()
         {
            return projectType;
         }

         @Override
         public Map<ValidationId, ValidationInfo> getValidationInfoList()
         {
            return null;
         }
      };
   }

   @Test
   public void onPageSizeClick()
   {
      presenter.onBind();
      presenter.onPageSizeClick(99);

      assertThat(configHolder.getState().getDocumentListPageSize(), Matchers.equalTo(99));
      ArgumentCaptor<UserConfigChangeEvent> eventCaptor = ArgumentCaptor.forClass(UserConfigChangeEvent.class);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().getView(), Matchers.equalTo(MainView.Documents));
   }

   @Test
   public void onLoadDefaultOptions()
   {
      presenter.loadDefaultOptions();

      verify(display).setOptionsState(configHolder.getState());
      verify(eventBus).fireEvent(isA(UserConfigChangeEvent.class));
      verify(eventBus).fireEvent(isA(NotificationEvent.class));
   }
   
   @Test
   public void onPersistOption()
   {
      presenter.persistOptionChange();

      verify(userOptionsService).persistOptionChange(userOptionsService.getDocumentListOptions());
   }
   
   @Test
   public void onLoadSavedOption()
   {
      UserConfigHolder configHolder = new UserConfigHolder();
      configHolder.setDocumentListPageSize(100);
      configHolder.setShowError(true);

      LoadOptionsResult result = new LoadOptionsResult(configHolder.getState());

      presenter.loadOptions();

      ArgumentCaptor<LoadOptionsAction> actionCaptor = ArgumentCaptor.forClass(LoadOptionsAction.class);
      ArgumentCaptor<AsyncCallback> callbackCaptor = ArgumentCaptor.forClass(AsyncCallback.class);
      verify(dispatcher).execute(actionCaptor.capture(), callbackCaptor.capture());

      AsyncCallback callback = callbackCaptor.getValue();

      callback.onSuccess(result);
      assertThat(configHolder.getState().getDocumentListPageSize(), Matchers.equalTo(100));
      assertThat(configHolder.getState().isShowError(), Matchers.equalTo(true));

      verify(eventBus).fireEvent(isA(UserConfigChangeEvent.class));

      callback.onFailure(null);
      verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
   }
}

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
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zanata.webtrans.client.view.TransUnitChangeSourceLangDisplay;

@Test(groups = { "unit-tests" })
public class TransUnitChangeSourceLangPresenterTest
{
   private TransUnitChangeSourceLangPresenter presenter;
   @Mock
   private TransUnitChangeSourceLangDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private TransUnitChangeSourceLangPresenter transUnitSourceLangPresenter;
   @Mock
   private TransUnitChangeSourceLangDisplay transUnitSourceLangDisplay;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private UserOptionsService userOptionsService;

   private UserConfigHolder configHolder = new UserConfigHolder();

   private WorkspaceId workspaceId;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

      presenter = new TransUnitChangeSourceLangPresenter(display, eventBus, dispatcher, configHolder);

      workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug", ProjectType.Podir), LocaleId.EN_US);

      verify(display).setListener(presenter);
   }
   
   @Test
   public void onSourceLangListBoxOptionChanged()
   {
      configHolder.setSelectedReferenceForSourceLang("none");

      presenter.onSourceLangListBoxOptionChanged("en-US");

      assertThat(configHolder.getState().getSelectedReferenceForSourceLang(), Matchers.equalTo("en-US"));
      verify(eventBus).fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
   }
}

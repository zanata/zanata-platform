package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;
import static org.mockito.Mockito.mock;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.DocumentListOptionsDisplay;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.client.view.OptionsDisplay;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;

@Test(groups = { "unit-tests" })
public class OptionsPresenterTest
{
   private OptionsPresenter presenter;
   
   @Mock
   private EditorOptionsPresenter editorOptionsPresenter;
   @Mock
   private DocumentListOptionsPresenter documentListOptionsPresenter;
   @Mock
   private OptionsDisplay display;
   @Mock
   private EventBus eventBus;
   
   private UserConfigHolder configHolder = new UserConfigHolder();
   
   @Captor
   private ArgumentCaptor<UserConfigChangeEvent> eventCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

     presenter = new OptionsPresenter(display, eventBus, editorOptionsPresenter, documentListOptionsPresenter, configHolder);
     verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      // When:
      presenter.onBind();

      // Then:
      verify(editorOptionsPresenter).onBind();
      verify(documentListOptionsPresenter).onBind();
      
      verify(eventBus).addHandler(UserConfigChangeEvent.TYPE, presenter);
      verify(display).setShowErrorChk(configHolder.isShowError());
   }
   
   @Test
   public void onUnbind()
   {
      presenter.onUnbind();

      verify(editorOptionsPresenter).unbind();
      verify(documentListOptionsPresenter).unbind();
   }

   @Test
   public void setOptionsViewEditor()
   {
      EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
      when(editorOptionsPresenter.getDisplay()).thenReturn(widget);
      presenter.onBind();
      presenter.setOptionsView(MainView.Editor);
      
      verify(display).setOptions(editorOptionsPresenter.getDisplay().asWidget());
   }
   
   @Test
   public void setOptionsViewDocumentList()
   {
      DocumentListOptionsDisplay widget = mock(DocumentListOptionsDisplay.class);
      when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);
      presenter.onBind();
      presenter.setOptionsView(MainView.Documents);
      
      verify(display).setOptions(documentListOptionsPresenter.getDisplay().asWidget());
   }
   
   @Test
   public void setOptionsViewSearch()
   {
      presenter.onBind();
      presenter.setOptionsView(MainView.Search);
      
      verify(display).setOptions(null);
   }
   
   @Test
   public void onShowErrorsOptionChanged()
   {
      presenter.onBind();
      presenter.onShowErrorsOptionChanged(true);
      
      assertThat(configHolder.isShowError(), Matchers.equalTo(true));
   }

   @Test
   public void onLoadDefaultOptionsEditor()
   {
      EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
      when(editorOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Editor);
      presenter.loadDefaultOptions();

      verify(editorOptionsPresenter).loadDefaultOptions();
   }
   
   @Test
   public void onLoadDefaultOptionsDocument()
   {
      DocumentListOptionsDisplay widget = mock(DocumentListOptionsDisplay.class);
      when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Documents);
      presenter.loadDefaultOptions();

      verify(documentListOptionsPresenter).loadDefaultOptions();
   }
   
   @Test
   public void persistOptionChangeEditor()
   {
      EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
      when(editorOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Editor);
      presenter.persistOptionChange();

      verify(editorOptionsPresenter).persistOptionChange();
   }
   
   @Test
   public void persistOptionChangeDocument()
   {
      DocumentListOptionsDisplay widget = mock(DocumentListOptionsDisplay.class);
      when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Documents);
      presenter.persistOptionChange();

      verify(documentListOptionsPresenter).persistOptionChange();
   }
   
   @Test
   public void loadOptionsEditor()
   {
      EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
      when(editorOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Editor);
      presenter.loadOptions();

      verify(editorOptionsPresenter).loadOptions();
   }
   
   @Test
   public void loadOptionsDocument()
   {
      DocumentListOptionsDisplay widget = mock(DocumentListOptionsDisplay.class);
      when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Documents);
      presenter.loadOptions();

      verify(documentListOptionsPresenter).loadOptions();
   }
   
   @Test
   public void loadDefaultOptionsEditor()
   {
      EditorOptionsDisplay widget = mock(EditorOptionsDisplay.class);
      when(editorOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Editor);
      presenter.loadDefaultOptions();

      verify(editorOptionsPresenter).loadDefaultOptions();
   }
   
   @Test
   public void loadDefaultOptionsDocument()
   {
      DocumentListOptionsDisplay widget = mock(DocumentListOptionsDisplay.class);
      when(documentListOptionsPresenter.getDisplay()).thenReturn(widget);
      
      presenter.onBind();
      presenter.setOptionsView(MainView.Documents);
      presenter.loadDefaultOptions();

      verify(documentListOptionsPresenter).loadDefaultOptions();
   }
}

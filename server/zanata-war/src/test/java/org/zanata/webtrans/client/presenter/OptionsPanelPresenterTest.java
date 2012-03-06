package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.easymock.Capture;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.ButtonDisplayChangeEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.presenter.OptionsPanelPresenter.Display;
import org.zanata.webtrans.shared.model.WorkspaceContext;


import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

@Test(groups = { "unit-tests" })
public class OptionsPanelPresenterTest
{

   // object under test
   private OptionsPanelPresenter optionsPanelPresenter;

   //injected mocks
   private Display mockDisplay = createMock(Display.class);
   private EventBus mockEventBus = createMock(EventBus.class);
   private ValidationOptionsPresenter mockValidationDetailsPresenter = createMock(ValidationOptionsPresenter.class);
   private WorkspaceContext mockWorkspaceContext = createMock(WorkspaceContext.class);

   //filter checkboxes
   @SuppressWarnings("unchecked")
   private HasValue<Boolean> mockTranslatedChk = createMock(HasValue.class);
   @SuppressWarnings("unchecked")
   private HasValue<Boolean> mockNeedReviewChk = createMock(HasValue.class);
   @SuppressWarnings("unchecked")
   private HasValue<Boolean> mockUntranslatedChk = createMock(HasValue.class);

   //editor option checkboxes
   @SuppressWarnings("unchecked")
   private HasValue<Boolean> mockEditorButtonsChk = createMock(HasValue.class);
   @SuppressWarnings("unchecked")
   private HasValue<Boolean> mockEnterChk = createMock(HasValue.class);
   @SuppressWarnings("unchecked")
   private HasValue<Boolean> mockEscChk = createMock(HasValue.class);

   private HasChangeHandlers mockFilterOptionsSelect = createMock(HasChangeHandlers.class);


   //captures for checkbox value change handlers
   Capture<ValueChangeHandler<Boolean>> capturedEditorButtonsChkValueChangeEventHandler = new Capture<ValueChangeHandler<Boolean>>();
   Capture<ValueChangeHandler<Boolean>> capturedEnterChkValueChangeEventHandler = new Capture<ValueChangeHandler<Boolean>>();
   Capture<ValueChangeHandler<Boolean>> capturedEscChkValueChangeEventHandler = new Capture<ValueChangeHandler<Boolean>>();
   Capture<ValueChangeHandler<Boolean>> capturedTranslatedChkValueChangeEventHandler = new Capture<ValueChangeHandler<Boolean>>();
   Capture<ValueChangeHandler<Boolean>> capturedNeedReviewChkValueChangeEventHandler = new Capture<ValueChangeHandler<Boolean>>();
   Capture<ValueChangeHandler<Boolean>> capturedUntranslatedChkValueChangeEventHandler = new Capture<ValueChangeHandler<Boolean>>();
   Capture<ChangeHandler> capturedFilterOptionsSelectChangeHandler = new Capture<ChangeHandler>();

   Capture<FilterViewEventHandler> capturedFilterViewEventHandler = new Capture<FilterViewEventHandler>();
   Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdateEventHandler = new Capture<WorkspaceContextUpdateEventHandler>();

   Capture<ButtonDisplayChangeEvent> capturedButtonDisplayChangeEvent = new Capture<ButtonDisplayChangeEvent>();



   @BeforeMethod
   public void resetMocks()
   {
      resetAllMocks();
      resetAllCaptures();

      //new presenter to test
      optionsPanelPresenter = newOptionsPanelPresenter();
   }


   public void canBindEditableWorkspace()
   {
      boolean readOnlyWorkspace = false;
      expectBindMethodBehaviour(readOnlyWorkspace);

      replayGlobalMocks();

      //try to bind
      optionsPanelPresenter.bind();

      verifyAllMocks();
   }

   public void canBindReadOnlyWorkspace()
   {
      boolean readOnlyWorkspace = true;
      expectBindMethodBehaviour(readOnlyWorkspace);
      replayGlobalMocks();
      optionsPanelPresenter.bind();
      verifyAllMocks();
   }

   public void readOnlyWorkspaceContextUpdated()
   {
      boolean changeToReadonly = true;
      testWorkspaceReadonlyChange(changeToReadonly, true);
   }

   public void editableWorkspaceContextUpdated()
   {
      boolean changeToReadonly = false;
      boolean editorButtonsOptionChecked = true;
      testWorkspaceReadonlyChange(changeToReadonly, editorButtonsOptionChecked);
   }

   public void editableWorkspaceContextUpdatedWithHiddenButtons()
   {
      boolean changeToReadonly = false;
      boolean editorButtonsOptionChecked = false;
      testWorkspaceReadonlyChange(changeToReadonly, editorButtonsOptionChecked);
   }

   private void testWorkspaceReadonlyChange(boolean changeToReadonly, boolean editorButtonsOptionChecked)
   {
      //start as opposite state
      boolean startReadOnly = !changeToReadonly;
      boolean changeToEditable = !changeToReadonly;
      expectBindMethodBehaviour(startReadOnly);

      //expected response
      mockEventBus.fireEvent(and(capture(capturedButtonDisplayChangeEvent), isA(ButtonDisplayChangeEvent.class)));
      mockDisplay.setEditorOptionsVisible(changeToEditable);
      mockDisplay.setValidationOptionsVisible(changeToEditable);

      if (changeToEditable)
      {
         //should check button display option to decide whether to show them
         expect(mockEditorButtonsChk.getValue()).andReturn(editorButtonsOptionChecked).anyTimes();
      }

      //workspace context event to fire
      WorkspaceContextUpdateEvent workspaceContextChangeEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(workspaceContextChangeEvent.isReadOnly()).andReturn(changeToReadonly).anyTimes();
      replay(workspaceContextChangeEvent);

      replayGlobalMocks();
      optionsPanelPresenter.bind();
      //simulate event
      capturedWorkspaceContextUpdateEventHandler.getValue().onWorkspaceContextUpdated(workspaceContextChangeEvent);

      verifyAllMocks();
      //check that buttons are hidden/shown
      assertThat(capturedButtonDisplayChangeEvent.getValue().isShowButtons(), is(changeToEditable && editorButtonsOptionChecked));
   }


   //TODO add tests based on OptionsPanelPresenter's responsibilities

   //Responsibilities:

   //filterViewEvent when any filter checkbox changed, with current filter values
   //set filter checkboxes in response to filter event
   //(To remove?) hide modal navigation options when appropriate
   //fire events for editor config change (filters, modal navigation, buttons)?


   private void expectBindMethodBehaviour(boolean readOnlyWorkspace)
   {
      mockValidationDetailsPresenter.bind();
      expectLastCall().once();

      expect(mockWorkspaceContext.isReadOnly()).andReturn(readOnlyWorkspace).once();

      if (readOnlyWorkspace)
      {
         mockEventBus.fireEvent(and(capture(capturedButtonDisplayChangeEvent), isA(ButtonDisplayChangeEvent.class)));
         mockDisplay.setEditorOptionsVisible(false);
         mockDisplay.setValidationOptionsVisible(false);
      }

      expectRegisterFilterChangeHandlers();
      expectEventBusEventHandlerRegistrations();
      expectRegisterEditorOptionsChangeHandlers();
      expectSetDefaultEditorOptionsChkStates();

      expect(mockDisplay.getFilterOptionsSelect()).andReturn(mockFilterOptionsSelect).anyTimes();
      expect(mockFilterOptionsSelect.addChangeHandler(capture(capturedFilterOptionsSelectChangeHandler))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void expectSetDefaultEditorOptionsChkStates()
   {
      mockEditorButtonsChk.setValue(true, false);
      expectLastCall().once();
      mockEnterChk.setValue(false, false);
      expectLastCall().once();
      mockEscChk.setValue(false, false);
      expectLastCall().once();
   }

   private void expectEventBusEventHandlerRegistrations()
   {
      expect(mockEventBus.addHandler(eq(FilterViewEvent.getType()), and(capture(capturedFilterViewEventHandler), isA(FilterViewEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(WorkspaceContextUpdateEvent.getType()), and(capture(capturedWorkspaceContextUpdateEventHandler), isA(WorkspaceContextUpdateEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void expectRegisterEditorOptionsChangeHandlers()
   {
      expect(mockDisplay.getEditorButtonsChk()).andReturn(mockEditorButtonsChk).anyTimes();
      expect(mockDisplay.getEnterChk()).andReturn(mockEnterChk).anyTimes();
      expect(mockDisplay.getEscChk()).andReturn(mockEscChk).anyTimes();

      expect(mockEditorButtonsChk.addValueChangeHandler(capture(capturedEditorButtonsChkValueChangeEventHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEnterChk.addValueChangeHandler(capture(capturedEnterChkValueChangeEventHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEscChk.addValueChangeHandler(capture(capturedEscChkValueChangeEventHandler))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void expectRegisterFilterChangeHandlers()
   {
      expect(mockDisplay.getTranslatedChk()).andReturn(mockTranslatedChk).anyTimes();
      expect(mockDisplay.getNeedReviewChk()).andReturn(mockNeedReviewChk).anyTimes();
      expect(mockDisplay.getUntranslatedChk()).andReturn(mockUntranslatedChk).anyTimes();

      expect(mockTranslatedChk.addValueChangeHandler(capture(capturedTranslatedChkValueChangeEventHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockNeedReviewChk.addValueChangeHandler(capture(capturedNeedReviewChkValueChangeEventHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockUntranslatedChk.addValueChangeHandler(capture(capturedUntranslatedChkValueChangeEventHandler))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void resetAllMocks()
   {
      reset(mockDisplay, mockEventBus);
      reset(mockValidationDetailsPresenter, mockWorkspaceContext);
      reset(mockTranslatedChk, mockNeedReviewChk, mockUntranslatedChk);
      reset(mockEditorButtonsChk, mockEnterChk, mockEscChk);
      reset(mockFilterOptionsSelect);
   }

   private void resetAllCaptures()
   {
      capturedEditorButtonsChkValueChangeEventHandler.reset();
      capturedEnterChkValueChangeEventHandler.reset();
      capturedEscChkValueChangeEventHandler.reset();

      capturedTranslatedChkValueChangeEventHandler.reset();
      capturedNeedReviewChkValueChangeEventHandler.reset();
      capturedUntranslatedChkValueChangeEventHandler.reset();

      capturedFilterOptionsSelectChangeHandler.reset();

      capturedFilterViewEventHandler.reset();
      capturedWorkspaceContextUpdateEventHandler.reset();

      capturedButtonDisplayChangeEvent.reset();
   }

   private void replayGlobalMocks()
   {
      replay(mockDisplay, mockEventBus);
      replay(mockValidationDetailsPresenter, mockWorkspaceContext);
      replay(mockTranslatedChk, mockNeedReviewChk, mockUntranslatedChk);
      replay(mockEditorButtonsChk, mockEnterChk, mockEscChk);
      replay(mockFilterOptionsSelect);
   }

   private void verifyAllMocks()
   {
      verify(mockDisplay, mockEventBus);
      verify(mockValidationDetailsPresenter, mockWorkspaceContext);
      verify(mockTranslatedChk, mockNeedReviewChk, mockUntranslatedChk);
      verify(mockEditorButtonsChk, mockEnterChk, mockEscChk);
      verify(mockFilterOptionsSelect);
   }

   /**
    * instantiate a new {@link OptionsPanelPresenter} using appropriate mocks
    * 
    * @return newly constructed OptionsPanelPresenter
    */
   private OptionsPanelPresenter newOptionsPanelPresenter()
   {
      return new OptionsPanelPresenter(mockDisplay, mockEventBus, mockValidationDetailsPresenter, mockWorkspaceContext);
   }
}

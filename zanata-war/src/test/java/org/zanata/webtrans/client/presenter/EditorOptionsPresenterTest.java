package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEventHandler;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.presenter.EditorOptionsPresenter.Display;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

@Test(groups = { "unit-tests" })
public class EditorOptionsPresenterTest extends PresenterTest
{

   // object under test
   EditorOptionsPresenter optionsPanelPresenter;

   //injected mocks
   Display mockDisplay;
   EventBus mockEventBus;
   UserWorkspaceContext mockUserWorkspaceContext;
   ValidationOptionsPresenter mockValidationDetailsPresenter;
   WorkspaceContext mockWorkspaceContext;

   //filter checkboxes
   HasValue<Boolean> mockTranslatedChk;
   HasValue<Boolean> mockNeedReviewChk;
   HasValue<Boolean> mockUntranslatedChk;

   //editor option checkboxes
   HasValue<Boolean> mockEditorButtonsChk;
   HasValue<Boolean> mockEnterChk;
   HasValue<Boolean> mockEscChk;

   HasChangeHandlers mockFilterOptionsSelect;


   //captures for checkbox value change handlers
   Capture<ValueChangeHandler<Boolean>> capturedEditorButtonsChkValueChangeEventHandler;
   Capture<ValueChangeHandler<Boolean>> capturedEnterChkValueChangeEventHandler;
   Capture<ValueChangeHandler<Boolean>> capturedEscChkValueChangeEventHandler;
   Capture<ValueChangeHandler<Boolean>> capturedTranslatedChkValueChangeEventHandler;
   Capture<ValueChangeHandler<Boolean>> capturedNeedReviewChkValueChangeEventHandler;
   Capture<ValueChangeHandler<Boolean>> capturedUntranslatedChkValueChangeEventHandler;
   Capture<ChangeHandler> capturedNavigationOptionsSelectChangeHandler;

   Capture<FilterViewEventHandler> capturedFilterViewEventHandler;
   Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdateEventHandler;
   Capture<EnableModalNavigationEventHandler> capturedEnableModalNavigationEventHandler;

   Capture<FilterViewEvent> capturedFilterViewEvent;
   Capture<UserConfigChangeEvent> capturedUserConfigChangeEvent;

   UserConfigHolder configHolder;

   @BeforeClass
   public void createMocks()
   {
      createAllMocks();
      createAllCaptures();
   }

   @SuppressWarnings("unchecked")
   private void createAllMocks()
   {
      mockDisplay = createAndAddMock(Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockValidationDetailsPresenter = createAndAddMock(ValidationOptionsPresenter.class);
      mockWorkspaceContext = createAndAddMock(WorkspaceContext.class);
      mockTranslatedChk = createAndAddMock(HasValue.class);
      mockNeedReviewChk = createAndAddMock(HasValue.class);
      mockUntranslatedChk = createAndAddMock(HasValue.class);
      mockEditorButtonsChk = createAndAddMock(HasValue.class);
      mockEnterChk = createAndAddMock(HasValue.class);
      mockEscChk = createAndAddMock(HasValue.class);
      mockFilterOptionsSelect = createAndAddMock(HasChangeHandlers.class);
   }

   private void createAllCaptures()
   {
      capturedEditorButtonsChkValueChangeEventHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedEnterChkValueChangeEventHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedEscChkValueChangeEventHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedTranslatedChkValueChangeEventHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedNeedReviewChkValueChangeEventHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedUntranslatedChkValueChangeEventHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedNavigationOptionsSelectChangeHandler = addCapture(new Capture<ChangeHandler>());

      capturedFilterViewEventHandler = addCapture(new Capture<FilterViewEventHandler>());
      capturedWorkspaceContextUpdateEventHandler = addCapture(new Capture<WorkspaceContextUpdateEventHandler>());
      capturedEnableModalNavigationEventHandler = addCapture(new Capture<EnableModalNavigationEventHandler>());

      capturedFilterViewEvent = addCapture(new Capture<FilterViewEvent>());
      capturedUserConfigChangeEvent = addCapture(new Capture<UserConfigChangeEvent>());
   }

   @Override
   protected void resetTestObjects()
   {
      configHolder = new UserConfigHolder();
      optionsPanelPresenter = new EditorOptionsPresenter(mockDisplay, mockEventBus, mockUserWorkspaceContext, mockValidationDetailsPresenter, configHolder);
   }

   @BeforeMethod
   public void resetEverything()
   {
      resetAll();
   }


   public void canBindEditableWorkspace()
   {
      boolean readOnlyWorkspace = false;
      expectBindMethodBehaviour(readOnlyWorkspace);
      replayAllMocks();
      optionsPanelPresenter.bind();
      verifyAllMocks();
   }

   public void canBindReadOnlyWorkspace()
   {
      boolean readOnlyWorkspace = true;
      expectBindMethodBehaviour(readOnlyWorkspace);
      replayAllMocks();
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

   /**
    * Tests 3 different scenarios: setting workspace to read-only, setting
    * workspace to editable with buttons shown, and setting workspace editable
    * with buttons hidden.
    * 
    * @param changeToReadonly
    * @param editorButtonsOptionChecked
    */
   private void testWorkspaceReadonlyChange(boolean changeToReadonly, boolean editorButtonsOptionChecked)
   {
      //start as opposite state
      boolean startReadOnly = !changeToReadonly;
      boolean changeToEditable = !changeToReadonly;
      expectBindMethodBehaviour(startReadOnly);

      //expected response
      mockEventBus.fireEvent(and(capture(capturedUserConfigChangeEvent), isA(UserConfigChangeEvent.class)));
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(changeToReadonly).once();
      mockUserWorkspaceContext.setProjectActive(changeToReadonly);
      if (changeToEditable)
      {
         //should check button display option to decide whether to show them
         expect(mockEditorButtonsChk.getValue()).andReturn(editorButtonsOptionChecked).anyTimes();
      }

      //workspace context event to fire
      WorkspaceContextUpdateEvent workspaceContextChangeEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(workspaceContextChangeEvent.isProjectActive()).andReturn(changeToReadonly).anyTimes();
      replay(workspaceContextChangeEvent);

      replayAllMocks();
      optionsPanelPresenter.bind();
      //simulate event
      capturedWorkspaceContextUpdateEventHandler.getValue().onWorkspaceContextUpdated(workspaceContextChangeEvent);

      verifyAllMocks();
      //check that buttons are hidden/shown
      assertThat(configHolder.isDisplayButtons(), is(changeToEditable && editorButtonsOptionChecked));
   }

   public void filterViewApprovedCheckbox()
   {
      boolean transChecked = true;
      boolean needReviewChecked = false;
      boolean untransChecked = false;
      boolean eventValue = transChecked;
      testFilterCheckboxChange(eventValue, transChecked, needReviewChecked, untransChecked,
                               capturedTranslatedChkValueChangeEventHandler);
   }

   public void filterViewNeedReviewCheckbox()
   {
      boolean transChecked = false;
      boolean needReviewChecked = true;
      boolean untransChecked = false;
      boolean eventValue = needReviewChecked;
      testFilterCheckboxChange(eventValue, transChecked, needReviewChecked, untransChecked,
                               capturedNeedReviewChkValueChangeEventHandler);
   }

   public void filterViewUntranslatedCheckbox()
   {
      boolean transChecked = false;
      boolean needReviewChecked = false;
      boolean untransChecked = true;
      boolean eventValue = untransChecked;
      testFilterCheckboxChange(eventValue, transChecked, needReviewChecked, untransChecked,
                               capturedUntranslatedChkValueChangeEventHandler);
   }

   public void unfilterViewApprovedCheckbox()
   {
      boolean transChecked = false;
      boolean needReviewChecked = true;
      boolean untransChecked = true;
      boolean eventValue = transChecked;
      testFilterCheckboxChange(eventValue, transChecked, needReviewChecked, untransChecked,
                               capturedTranslatedChkValueChangeEventHandler);
   }

   public void unfilterViewNeedReviewCheckbox()
   {
      boolean transChecked = true;
      boolean needReviewChecked = false;
      boolean untransChecked = true;
      boolean eventValue = needReviewChecked;
      testFilterCheckboxChange(eventValue, transChecked, needReviewChecked, untransChecked,
                               capturedNeedReviewChkValueChangeEventHandler);
   }

   public void unfilterViewUntranslatedCheckbox()
   {
      boolean transChecked = true;
      boolean needReviewChecked = true;
      boolean untransChecked = false;
      boolean eventValue = untransChecked;
      testFilterCheckboxChange(eventValue, transChecked, needReviewChecked, untransChecked,
                               capturedUntranslatedChkValueChangeEventHandler);
   }

   /**
    * Tests that changing filter checkboxes fires the correct event, allowing
    * different filter option combinations to be specified. eventValue must be
    * the same as *Checked for the checkbox under test (i.e. the checkbox for
    * which capturedCheckboxChangeHandler is the handler).
    * 
    * @param eventValue value to return from checkbox {@link ValueChangeEvent}.
    *           Must match the value for *Checked parameter for checkbox under
    *           test.
    * @param transChecked
    * @param needReviewChecked
    * @param untransChecked
    * @param capturedCheckboxChangeHandler handler to handle the
    *           {@link ValueChangeEvent}
    */
   private void testFilterCheckboxChange(boolean eventValue, boolean transChecked, boolean needReviewChecked, boolean untransChecked, Capture<ValueChangeHandler<Boolean>> capturedCheckboxChangeHandler)
   {
      expectBindMethodBehaviour(false);
      mockEventBus.fireEvent(and(capture(capturedFilterViewEvent), isA(FilterViewEvent.class)));

      //simulate current state of checkboxes
      expect(mockTranslatedChk.getValue()).andReturn(transChecked).anyTimes();
      expect(mockNeedReviewChk.getValue()).andReturn(needReviewChecked).anyTimes();
      expect(mockUntranslatedChk.getValue()).andReturn(untransChecked).anyTimes();

      replayAllMocks();

      optionsPanelPresenter.bind();

      //simulate checkbox check/uncheck
      @SuppressWarnings("unchecked")
      ValueChangeEvent<Boolean> event = createMock(ValueChangeEvent.class);
      expect(event.getValue()).andReturn(eventValue).anyTimes();
      replay(event);
      capturedCheckboxChangeHandler.getValue().onValueChange(event);

      verifyAllMocks();
      //check that filter view event has correct flags
      assertThat(capturedFilterViewEvent.getValue().isFilterTranslated(), is(transChecked));
      assertThat(capturedFilterViewEvent.getValue().isFilterNeedReview(), is(needReviewChecked));
      assertThat(capturedFilterViewEvent.getValue().isFilterUntranslated(), is(untransChecked));
      assertThat("isCancelFilter should always be false when manipulating filter checkboxes",
                 capturedFilterViewEvent.getValue().isCancelFilter(), is(false));
   }


   public void filterChangeFilterAll()
   {
      boolean filterTranslated = true;
      boolean filterNeedReview = true;
      boolean filterUntranslated = true;
      boolean cancelFilter = false;

      //modal nav options visible when all or no filters selected
      boolean expectShowNavOptions = true;

      testFilterViewEventResponse(filterTranslated, filterNeedReview, filterUntranslated, cancelFilter, expectShowNavOptions);
   }

   public void filterChangeFilterNone()
   {
      boolean filterTranslated = false;
      boolean filterNeedReview = false;
      boolean filterUntranslated = false;
      boolean cancelFilter = false;

      //modal nav options visible when all or no filters selected
      boolean expectShowNavOptions = true;

      testFilterViewEventResponse(filterTranslated, filterNeedReview, filterUntranslated, cancelFilter, expectShowNavOptions);
   }

   public void filterChangeFilterSome()
   {
      boolean filterTranslated = true;
      boolean filterNeedReview = false;
      boolean filterUntranslated = true;
      boolean cancelFilter = false;

      //modal nav options hidden when 3 filter flags not all same
      boolean expectShowNavOptions = false;

      testFilterViewEventResponse(filterTranslated, filterNeedReview, filterUntranslated, cancelFilter, expectShowNavOptions);
   }

   public void filterChangeCancelFilter()
   {
      //expect to run value setters when cancelFilter is true
      boolean cancelFilter = true;

      boolean filterTranslated = true;
      boolean filterNeedReview = false;
      boolean filterUntranslated = true;

      //modal nav options hidden when  3 filter flags not all same
      boolean expectShowNavOptions = false;

      testFilterViewEventResponse(filterTranslated, filterNeedReview, filterUntranslated, cancelFilter, expectShowNavOptions);
   }

   /**
    * Test that navigation options are shown or hidden in response to a
    * {@link FilterViewEvent} with the specified flag values.
    * 
    * @param filterTranslated
    * @param filterNeedReview
    * @param filterUntranslated
    * @param cancelFilter
    * @param expectShowNavOptions
    */
   private void testFilterViewEventResponse(boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated,
                                            boolean cancelFilter, boolean expectShowNavOptions)
   {
      expectBindMethodBehaviour(false);

      FilterViewEvent event = createMock(FilterViewEvent.class);
      expect(event.isFilterTranslated()).andReturn(filterTranslated).anyTimes();
      expect(event.isFilterNeedReview()).andReturn(filterNeedReview).anyTimes();
      expect(event.isFilterUntranslated()).andReturn(filterUntranslated).anyTimes();
      expect(event.isCancelFilter()).andReturn(cancelFilter).anyTimes();

      if (cancelFilter)
      {
         //should run value setters without events when cancelFilter is true
         boolean fireEvents = false;
         mockTranslatedChk.setValue(filterTranslated, fireEvents);
         mockNeedReviewChk.setValue(filterNeedReview, fireEvents);
         mockUntranslatedChk.setValue(filterUntranslated, fireEvents);
      }

      replay(event);
      replayAllMocks();

      optionsPanelPresenter.bind();
      capturedFilterViewEventHandler.getValue().onFilterView(event);

      verifyAllMocks();
   }

   public void editorButtonsOptionChecked()
   {
      boolean buttonCheckValue = true;
      testEditorButtonsOptionCheckEvent(buttonCheckValue);
   }

   public void editorButtonsOptionUnchecked()
   {
      boolean buttonCheckValue = false;
      testEditorButtonsOptionCheckEvent(buttonCheckValue);
   }


   private void testEditorButtonsOptionCheckEvent(boolean editorButtonsOptionCheckValue)
   {
      expectBindMethodBehaviour(false);

      @SuppressWarnings("unchecked")
      ValueChangeEvent<Boolean> event = createMock(ValueChangeEvent.class);
      expect(event.getValue()).andReturn(editorButtonsOptionCheckValue).anyTimes();
      mockEventBus.fireEvent(and(capture(capturedUserConfigChangeEvent), isA(UserConfigChangeEvent.class)));

      replay(event);
      replayAllMocks();

      optionsPanelPresenter.bind();
      capturedEditorButtonsChkValueChangeEventHandler.getValue().onValueChange(event);

      verifyAllMocks();
      assertThat(configHolder.isDisplayButtons(), is(editorButtonsOptionCheckValue));
   }

   public void enterOptionChecked()
   {
      boolean enterOptionCheckValue = true;
      Capture<ValueChangeHandler<Boolean>> enterCheckboxValueChangeHandler = capturedEnterChkValueChangeEventHandler;
      testOptionCheckTriggersUserConfigEvent(enterOptionCheckValue, enterCheckboxValueChangeHandler);
      assertThat(configHolder.isEnterSavesApproved(), is(enterOptionCheckValue));

   }

   public void enterOptionUnchecked()
   {
      boolean enterOptionCheckValue = false;
      Capture<ValueChangeHandler<Boolean>> enterCheckboxValueChangeHandler = capturedEnterChkValueChangeEventHandler;
      testOptionCheckTriggersUserConfigEvent(enterOptionCheckValue, enterCheckboxValueChangeHandler);
      assertThat(configHolder.isEnterSavesApproved(), is(enterOptionCheckValue));
   }

   public void escOptionChecked()
   {
      boolean escOptionCheckValue = true;
      Capture<ValueChangeHandler<Boolean>> escCheckboxValueChangeHandler = capturedEscChkValueChangeEventHandler;
      testOptionCheckTriggersUserConfigEvent(escOptionCheckValue, escCheckboxValueChangeHandler);
      assertThat(configHolder.isEscClosesEditor(), is(escOptionCheckValue));

   }

   public void escOptionUnchecked()
   {
      boolean escOptionCheckValue = false;
      Capture<ValueChangeHandler<Boolean>> escCheckboxValueChangeHandler = capturedEscChkValueChangeEventHandler;
      testOptionCheckTriggersUserConfigEvent(escOptionCheckValue, escCheckboxValueChangeHandler);
      assertThat(configHolder.isEscClosesEditor(), is(escOptionCheckValue));
   }

   /**
    * Test that user config event is generated in response to
    * checking/unchecking an editor option checkbox.
    *
    * @param optionCheckValue new value used for value change event
    * @param checkboxValueChangeHandler handler for the checkbox under test
    */
   private void testOptionCheckTriggersUserConfigEvent(boolean optionCheckValue, Capture<ValueChangeHandler<Boolean>> checkboxValueChangeHandler)
   {
      expectBindMethodBehaviour(false);

      @SuppressWarnings("unchecked")
      ValueChangeEvent<Boolean> event = createMock(ValueChangeEvent.class);
      expect(event.getValue()).andReturn(optionCheckValue).anyTimes();

      mockEventBus.fireEvent(and(capture(capturedUserConfigChangeEvent), isA(UserConfigChangeEvent.class)));

      replay(event);
      replayAllMocks();

      optionsPanelPresenter.bind();
      checkboxValueChangeHandler.getValue().onValueChange(event);

      verifyAllMocks();
   }


   public void selectFuzzyNavigation()
   {
      String selectedFilter = "F";
      boolean expectFuzzyNavigation = true;
      boolean expectUntranslatedNavigation = false;
      testNavigationTypeSelection(selectedFilter, expectFuzzyNavigation, expectUntranslatedNavigation);
   }

   public void selectUntranslatedNavigation()
   {
      String selectedFilter = "U";
      boolean expectFuzzyNavigation = false;
      boolean expectUntranslatedNavigation = true;
      testNavigationTypeSelection(selectedFilter, expectFuzzyNavigation, expectUntranslatedNavigation);
   }

   public void selectFuzzyUntranslatedNavigation()
   {
      String selectedFilter = "FU";
      boolean expectFuzzyNavigation = true;
      boolean expectUntranslatedNavigation = true;
      testNavigationTypeSelection(selectedFilter, expectFuzzyNavigation, expectUntranslatedNavigation);
   }

   /**
    * Checks that a user config change event with correct values is fired in
    * response to a specific navigation type being selected.
    * 
    * @param selectedFilter
    * @param expectFuzzyNavigation
    * @param expectUntranslatedNavigation
    */
   private void testNavigationTypeSelection(String selectedFilter, boolean expectFuzzyNavigation, boolean expectUntranslatedNavigation)
   {
      expectBindMethodBehaviour(false);
      ChangeEvent event = createMock(ChangeEvent.class);
      expect(mockDisplay.getSelectedFilter()).andReturn(selectedFilter).anyTimes();
      mockEventBus.fireEvent(and(capture(capturedUserConfigChangeEvent), isA(UserConfigChangeEvent.class)));

      replay(event);
      replayAllMocks();

      optionsPanelPresenter.bind();
      capturedNavigationOptionsSelectChangeHandler.getValue().onChange(event);

      verifyAllMocks();
      assertThat(configHolder.isButtonFuzzy(), is(expectFuzzyNavigation));
      assertThat(configHolder.isButtonUntranslated(), is(expectUntranslatedNavigation));
   }


   /**
    * Set up expectations for all the default bind behaviour, with variation for
    * workspace starting as read-only
    * 
    * @param readOnlyWorkspace
    */
   private void expectBindMethodBehaviour(boolean readOnlyWorkspace)
   {
      mockValidationDetailsPresenter.bind();

      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(readOnlyWorkspace).once();

      if (readOnlyWorkspace)
      {
         mockEventBus.fireEvent(and(capture(capturedUserConfigChangeEvent), isA(UserConfigChangeEvent.class)));
      }

      expectRegisterFilterChangeHandlers();
      expectEventBusEventHandlerRegistrations();
      expectRegisterEditorOptionsChangeHandlers();
      expectSetDefaultEditorOptionsChkStates();

      expect(mockDisplay.getModalNavigationOptionsSelect()).andReturn(mockFilterOptionsSelect).anyTimes();
      expect(mockFilterOptionsSelect.addChangeHandler(capture(capturedNavigationOptionsSelectChangeHandler))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void expectSetDefaultEditorOptionsChkStates()
   {
      mockEditorButtonsChk.setValue(true, false);
      mockEnterChk.setValue(false, false);
      mockEscChk.setValue(false, false);
   }

   private void expectEventBusEventHandlerRegistrations()
   {
      expect(mockEventBus.addHandler(eq(FilterViewEvent.getType()), and(capture(capturedFilterViewEventHandler), isA(FilterViewEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(WorkspaceContextUpdateEvent.getType()), and(capture(capturedWorkspaceContextUpdateEventHandler), isA(WorkspaceContextUpdateEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(EnableModalNavigationEvent.getType()), and(capture(capturedEnableModalNavigationEventHandler), isA(EnableModalNavigationEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
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

   @Override
   protected void setDefaultBindExpectations()
   {
      // Not used as boolean required, see .expectBindMethodBehaviour(boolean)
   }
}

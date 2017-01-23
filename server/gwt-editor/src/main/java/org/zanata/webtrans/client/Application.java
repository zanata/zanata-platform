package org.zanata.webtrans.client;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.LocaleId;
import org.zanata.util.CoverageIgnore;
import org.zanata.webtrans.client.EventProcessor.StartCallback;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.gin.WebTransGinjector;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.UserScriptCallbackHook;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;
import org.zanata.webtrans.shared.rpc.NoOpResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Stopwatch;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {
    private static final WebTransGinjector injector = GWT
            .create(WebTransGinjector.class);
    private static final String APP_LOAD_ERROR_CSS_CLASS = "AppLoadError";
    private static final String CONTENT_DIV_ID = "container";

    private static WorkspaceId workspaceId;
    private static UserWorkspaceContext userWorkspaceContext;
    private static Identity identity;

    private UncaughtExceptionHandlerImpl exceptionHandler;

    public void onModuleLoad() {
        Log.info("Loading Zanata Web Editor...");
        exceptionHandler =
                new UncaughtExceptionHandlerImpl(injector.getDispatcher(),
                        injector.getUserConfig());
        GWT.setUncaughtExceptionHandler(exceptionHandler);

        injector.getDispatcher().execute(
                new ActivateWorkspaceAction(getWorkspaceId()),
                new AsyncCallback<ActivateWorkspaceResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof AuthenticationError) {
                            Log.error("Authentication error.", caught);
                            redirectToLogin();
                        } else if (caught instanceof NoSuchWorkspaceException) {
                            Log.error("Invalid workspace", caught);
                            String errorMessage;
                            errorMessage =
                                    "Invalid Workspace. "
                                            + caught.getLocalizedMessage()
                                            + ". Try opening the workspace from the link on the project page.";
                            showErrorWithLink(errorMessage, caught);
                        } else {
                            Log.error("An unexpected Error occurred", caught);
                            showErrorWithLink("An unexpected Error occurred: "
                                    + caught.getMessage(), caught);
                        }
                    }

                    @Override
                    public void onSuccess(final ActivateWorkspaceResult result) {
                        userWorkspaceContext = result.getUserWorkspaceContext();
                        identity = result.getIdentity();
                        injector.getDispatcher().setIdentity(identity);
                        injector.getDispatcher().setUserWorkspaceContext(
                                userWorkspaceContext);
                        injector.getDispatcher().setEventBus(
                                injector.getEventBus());
                        injector.getUserConfig().setState(
                                result.getStoredUserConfiguration());
                        injector.getValidationService().setValidationRules(
                                result.getValidationStates());

                        startApp();
                    }

                });

    }

    public static void exitWorkspace() {
        injector.getDispatcher().execute(
                new ExitWorkspaceAction(identity.getPerson()),
                new NoOpAsyncCallback<NoOpResult>());
    }

    private void startApp() {
        // When user close the workspace, send ExitWorkSpaceAction
        Window.addWindowClosingHandler(new Window.ClosingHandler() {
            @Override
            public void onWindowClosing(ClosingEvent event) {
                exitWorkspace();
            }
        });

        final EventProcessor eventProcessor = injector.getEventProcessor();
        eventProcessor.start(new StartCallback() {
            @Override
            public void onSuccess(String connectionId) {
                // tell server the ConnectionId for this EditorClientId
                injector.getDispatcher().execute(
                        new EventServiceConnectedAction(connectionId),
                        new AsyncCallback<NoOpResult>() {
                            @Override
                            public void onFailure(Throwable e) {
                                showErrorWithLink(
                                        "Server communication failed...", e);
                            }

                            @Override
                            public void onSuccess(NoOpResult result) {
                                delayedStartApp();
                            }
                        });
            }

            @Override
            public void onFailure(Throwable e) {
                showErrorWithLink("Failed to start Event Service...", e);
            }
        });

        Window.enableScrolling(true);
    }

    private void delayedStartApp() {
        final AppPresenter appPresenter = injector.getAppPresenter();
        final DocumentListPresenter documentListPresenter =
                injector.getDocumentListPresenter();
        RootPanel.get(CONTENT_DIV_ID).add(appPresenter.getDisplay().asWidget());
        appPresenter.bind();
        Window.enableScrolling(true);
        // eager load document list
        final EventBus eventBus = injector.getEventBus();

        GetDocumentList action =
                new GetDocumentList(injector.getLocation().getQueryDocuments());

        documentListPresenter.showLoading(true);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        injector.getDispatcher().execute(action,
                new AsyncCallback<GetDocumentListResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Error,
                                "Failed to load documents"));
                        documentListPresenter.showLoading(false);
                        stopwatch.stop();
                    }

                    @Override
                    public void onSuccess(GetDocumentListResult result) {
                        final List<DocumentInfo> documents =
                                result.getDocuments();
                        Log.info("Received doc list for "
                                + result.getProjectIterationId() + ": "
                                + documents.size()
                                + " elements, loading time: "
                                + stopwatch.elapsed(MILLISECONDS) + "ms");
                        documentListPresenter.setDocuments(documents);

                        History history = injector.getHistory();
                        history.addValueChangeHandler(injector
                                .getHistoryEventHandlerService());
                        Log.info("=========== now firing current history state =========== ");
                        history.fireCurrentHistoryState();

                        exceptionHandler.setAppPresenter(appPresenter);
                        exceptionHandler.setTargetContentsPresenter(injector
                                .getTargetContentsPresenter());

                        documentListPresenter.queryStats();
                        documentListPresenter.showLoading(false);
                        injector.getTransUnitSaveService().init();

                        stopwatch.stop();
                    }
                });

        registerUserScriptCallbackHook();
    }

    private void registerUserScriptCallbackHook() {
        final EventBus eventBus = injector.getEventBus();
        UserScriptCallbackHook hook = new UserScriptCallbackHook();
        eventBus.addHandler(TransUnitUpdatedEvent.getType(), hook);
    }

    public static ProjectIterationId getProjectIterationId() {
        String projectSlug = Window.Location.getParameter("project");
        String iterationSlug = Window.Location.getParameter("iteration");
        if (projectSlug == null || iterationSlug == null) {
            return null;
        }
        return new ProjectIterationId(projectSlug, iterationSlug, null);
    }

    public static LocaleId getLocaleId() {
        String localeId = Window.Location.getParameter("localeId");
        return localeId == null ? null : new LocaleId(localeId);
    }

    public static void redirectToLogin() {
        redirectToUrl(getModuleParentBaseUrl() + "account/sign_in?continue="
                + URL.encodeQueryString(Window.Location.getHref()));
    }

    public static void redirectToLogout() {
        redirectToUrl(getModuleParentBaseUrl() + "account/sign_out");
    }

    public static String getProjectHomeURL(WorkspaceId workspaceId) {
        return getModuleParentBaseUrl() + "project/view/"
                + workspaceId.getProjectIterationId().getProjectSlug();
    }

    public static String getVersionHomeURL(WorkspaceId workspaceId) {
        return getModuleParentBaseUrl() + "iteration/view/"
                + workspaceId.getProjectIterationId().getProjectSlug() + "/"
                + workspaceId.getProjectIterationId().getIterationSlug();
    }

    public static String getVersionFilesURL(WorkspaceId workspaceId) {
        return getModuleParentBaseUrl() + "iteration/files/"
                + workspaceId.getProjectIterationId().getProjectSlug() + "/"
                + workspaceId.getProjectIterationId().getIterationSlug() + "/"
                + workspaceId.getLocaleId().getId();
    }

    public static String getEditorUrl(WorkspaceId workspaceId,
            String tokens) {
        ProjectIterationId versionId = workspaceId.getProjectIterationId();
        String hash = "#" + tokens;

        String locale = Window.Location.getParameter("locale");
        return getEditorUrl(versionId.getProjectSlug(),
                versionId.getIterationSlug(), locale,
                workspaceId.getLocaleId().getId(), hash);
    }

    public static String getEditorUrl(String projectSlug, String versionSlug,
        String srcLocale, String localeId, String hash) {
        return getModuleParentBaseUrl() + "webtrans/translate?project=" +
            projectSlug + "&iteration=" + versionSlug +
            "&localeId=" + localeId + "&locale=" + srcLocale + hash;
    }

    public static String getNewEditorLink(WorkspaceId workspaceId, String docId) {
        return getModuleParentBaseUrl() + "project/translate/"
                + workspaceId.getProjectIterationId().getProjectSlug() + "/v/"
                + workspaceId.getProjectIterationId().getIterationSlug()
                + "/" + docId
                + getDswidQuery() + "&lang=" + workspaceId.getLocaleId().getId();
    }

    private static String getDswidQuery() {
        return "?dswid=" + injector.getLocation().getParameter("dswid");
    }

    public static String getFileDownloadURL(WorkspaceId workspaceId,
            String downloadExtension) {
        return getModuleParentBaseUrl() + "rest/file/translation/"
                + workspaceId.getProjectIterationId().getProjectSlug() + "/"
                + workspaceId.getProjectIterationId().getIterationSlug() + "/"
                + workspaceId.getLocaleId().getId() + "/" + downloadExtension;
    }

    public static String getUserProfileURL(String username) {
        return getModuleParentBaseUrl() + "profile/view/" + username;
    }

    public static String getAllFilesDownloadURL(String downloadId) {
        return getModuleParentBaseUrl() + "rest/file/download/" + downloadId;
    }

    public static boolean isProjectGlossary(String qualifiedName) {
        return qualifiedName.startsWith("project");
    }

    @CoverageIgnore("JSNI")
    public static native void redirectToUrl(String url)
        /*-{
    $wnd.location = url;
    }-*/;

    public static WorkspaceId getWorkspaceId() {
        if (workspaceId == null) {
            workspaceId =
                    new WorkspaceId(getProjectIterationId(), getLocaleId());
        }
        return workspaceId;
    }

    public static UserWorkspaceContext getUserWorkspaceContext() {
        return userWorkspaceContext;
    }

    public static Identity getIdentity() {
        return identity;
    }

    public static String getModuleParentBaseUrl() {
        return GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "");
    }

    /**
     * Display an error message instead of the web app. Shows a link if both
     * text and url are provided. Provides a stack trace if a {@link Throwable}
     * is provided.
     *
     * @param message
     *            to display
     * @param e
     *            non-null to provide a stack trace in an expandable view.
     */
    private static void showErrorWithLink(String message, Throwable e) {
        Label messageLabel = new Label(message);
        messageLabel.getElement().addClassName(APP_LOAD_ERROR_CSS_CLASS);
        FlowPanel layoutPanel = new FlowPanel();
        layoutPanel.add(messageLabel);

        if (!GWT.isProdMode() && e != null) {
            String stackTrace =
                    UncaughtExceptionHandlerImpl.buildStackTraceMessages(e);
            SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
            safeHtmlBuilder.appendHtmlConstant("<pre>")
                    .appendEscaped(stackTrace).appendHtmlConstant("</pre>");
            Label stackTraceLabel =
                    new Label(safeHtmlBuilder.toSafeHtml().asString());
            DisclosurePanel stackTracePanel =
                    new DisclosurePanel("More detail:");
            stackTracePanel.getElement().addClassName(APP_LOAD_ERROR_CSS_CLASS);
            stackTracePanel.add(stackTraceLabel);
            layoutPanel.add(stackTracePanel);
        }

        RootPanel.get(CONTENT_DIV_ID).add(layoutPanel);
    }
}

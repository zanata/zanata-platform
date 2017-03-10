package org.zanata.service.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.common.util.Strings;
import org.zanata.async.Async;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.events.WebhookEvent;
import org.zanata.events.WebhookEventType;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.ProjectRole;
import org.zanata.model.WebHook;
import org.zanata.model.type.WebhookType;
import org.zanata.security.annotations.Authenticated;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.util.UrlUtil;
import org.zanata.webhook.events.DocumentMilestoneEvent;
import org.zanata.webhook.events.DocumentStatsEvent;
import org.zanata.webhook.events.ProjectMaintainerChangedEvent;
import org.zanata.webhook.events.SourceDocumentChangedEvent;
import org.zanata.webhook.events.TestEvent;
import org.zanata.webhook.events.ManuallyTriggeredEvent;
import org.zanata.webhook.events.VersionChangedEvent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("webhookServiceImpl")
@RequestScoped
public class WebhookServiceImpl implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WebhookServiceImpl.class);

    @Inject
    private Messages msgs;
    @Inject
    private Event<WebhookEvent> webhookEventEvent;
    @Inject
    @Authenticated
    private HAccount authenticatedUser;
    private static final int URL_MAX_LENGTH = 255;
    @Inject
    @ServerPath
    private String serverUrl;

    /**
     * Need @Async annotation for TransactionPhase.AFTER_SUCCESS event
     */
    @Async
    public void onPublishWebhook(@Observes(
            during = TransactionPhase.AFTER_SUCCESS) WebhookEvent event) {
        WebHooksPublisher.publish(event.getUrl(), event.getType(),
                Optional.ofNullable(event.getSecret()));
    }

    /**
     * Process TestEvent
     */
    public void processTestEvent(String username, String projectSlug,
            String url, String secret) {
        TestEvent event = new TestEvent(username, projectSlug);
        webhookEventEvent.fire(new WebhookEvent(url, secret, event));
    }

    /**
     * Process VersionChangedEvent
     */
    public void processWebhookVersionChanged(String projectSlug,
            String versionSlug, List<WebHook> webHooks,
            VersionChangedEvent.ChangeType changeType) {
        List<WebHook> versionWebhooks =
                filterWebhookByType(webHooks, WebhookType.VersionChangedEvent);
        if (versionWebhooks.isEmpty()) {
            return;
        }
        VersionChangedEvent event =
                new VersionChangedEvent(projectSlug, versionSlug, changeType);
        publishWebhooks(versionWebhooks, event);
    }

    /**
     * Process ProjectMaintainerChangedEvent
     */
    public void processWebhookMaintainerChanged(String projectSlug,
            String username, ProjectRole role, List<WebHook> webHooks,
            ProjectMaintainerChangedEvent.ChangeType changeType) {
        List<WebHook> maintainerWebhooks = filterWebhookByType(webHooks,
                WebhookType.ProjectMaintainerChangedEvent);
        if (maintainerWebhooks.isEmpty()) {
            return;
        }
        ProjectMaintainerChangedEvent event = new ProjectMaintainerChangedEvent(
                projectSlug, username, role, changeType);
        publishWebhooks(maintainerWebhooks, event);
    }

    /**
     * Process SourceDocumentChangedEvent
     */
    public void processWebhookSourceDocumentChanged(String project,
            String version, String docId, List<WebHook> webHooks,
            SourceDocumentChangedEvent.ChangeType changeType) {
        List<WebHook> eventWebhooks = filterWebhookByType(webHooks,
                WebhookType.SourceDocumentChangedEvent);
        if (eventWebhooks.isEmpty()) {
            return;
        }
        SourceDocumentChangedEvent event = new SourceDocumentChangedEvent(
                project, version, docId, changeType);
        publishWebhooks(eventWebhooks, event);
    }

    /**
     * Process DocumentMilestoneEvent
     */
    public void processDocumentMilestone(String projectSlug, String versionSlug,
            String docId, LocaleId localeId, String message, String editorUrl,
            List<WebHook> webHooks) {
        DocumentMilestoneEvent milestoneEvent = new DocumentMilestoneEvent(
                projectSlug, versionSlug, docId, localeId, message, editorUrl);
        publishWebhooks(webHooks, milestoneEvent);
    }

    /**
     * Process DocumentStatsEvent
     */
    public void processDocumentStats(String username, String projectSlug,
            String versionSlug, String docId, LocaleId localeId,
            Map<ContentState, Long> wordDeltasByState, List<WebHook> webHooks) {
        DocumentStatsEvent statsEvent = new DocumentStatsEvent(username,
                projectSlug, versionSlug, docId, localeId, wordDeltasByState);
        publishWebhooks(webHooks, statsEvent);
    }

    /**
     * Process ManuallyTriggeredEvent
     */
    public void processManualEvent(String projectSlug, String versionSlug,
            LocaleId localeId, List<WebHook> webHooks) {
        ManuallyTriggeredEvent event = new ManuallyTriggeredEvent(serverUrl,
                authenticatedUser.getUsername(), projectSlug, versionSlug,
                localeId);
        publishWebhooks(webHooks, event);
    }

    public List<WebhookTypeItem> getAvailableWebhookTypes() {
        WebhookTypeItem docMilestone = new WebhookTypeItem(
                WebhookType.DocumentMilestoneEvent,
                msgs.get("jsf.webhookType.DocumentMilestoneEvent.desc"));
        WebhookTypeItem stats =
                new WebhookTypeItem(WebhookType.DocumentStatsEvent,
                        msgs.get("jsf.webhookType.DocumentStatsEvent.desc"));
        WebhookTypeItem version =
                new WebhookTypeItem(WebhookType.VersionChangedEvent,
                        msgs.get("jsf.webhookType.VersionChangedEvent.desc"));
        WebhookTypeItem maintainer = new WebhookTypeItem(
                WebhookType.ProjectMaintainerChangedEvent,
                msgs.get("jsf.webhookType.ProjectMaintainerChangedEvent.desc"));
        WebhookTypeItem srcDoc = new WebhookTypeItem(
                WebhookType.SourceDocumentChangedEvent,
                msgs.get("jsf.webhookType.SourceDocumentChangedEvent.desc"));
        WebhookTypeItem manualEvent = new WebhookTypeItem(
                WebhookType.ManuallyTriggeredEvent,
                msgs.get("jsf.webhookType.ManuallyTriggeredEvent.desc"));
        return Lists.newArrayList(docMilestone, stats, version, maintainer,
                srcDoc, manualEvent);
    }

    public List<String> getDisplayNames(Set<WebhookType> types) {
        return types.stream().map(WebhookType::getDisplayName)
                .collect(Collectors.toList());
    }

    public String getTypesAsString(WebHook webHook) {
        if (webHook == null) {
            return "";
        }
        List<String> results = webHook.getTypes().stream().map(Enum::name)
                .collect(Collectors.toList());
        return Strings.join(results, ",");
    }

    public boolean isValidUrl(String url) {
        return UrlUtil.isValidUrl(url)
                && StringUtils.length(url) <= URL_MAX_LENGTH;
    }

    public static Set<WebhookType> getTypesFromString(String strTypes) {
        return new HashSet(Lists.transform(
                Lists.newArrayList(strTypes.split(",")), convertToWebHookType));
    }

    private static Function convertToWebHookType =
            new Function<String, WebhookType>() {

                @Override
                public WebhookType apply(String input) {
                    return WebhookType.valueOf(input);
                }
            };

    /**
     * Object for all available webhook list
     */
    public static final class WebhookTypeItem {
        private WebhookType type;
        private String description;

        public WebhookTypeItem(WebhookType webhookType, String desc) {
            this.type = webhookType;
            this.description = desc;
        }

        public WebhookType getType() {
            return this.type;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private void publishWebhooks(List<WebHook> webHooks,
            WebhookEventType event) {
        for (WebHook webhook : webHooks) {
            webhookEventEvent.fire(new WebhookEvent(webhook.getUrl(),
                    webhook.getSecret(), event));
        }
    }

    private List<WebHook> filterWebhookByType(List<WebHook> webHooks,
            WebhookType webhookType) {
        return webHooks.stream()
                .filter(webHook -> webHook.getTypes().contains(webhookType))
                .collect(Collectors.toList());
    }
}

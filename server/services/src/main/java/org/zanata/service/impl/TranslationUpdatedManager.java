package org.zanata.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.Async;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.model.type.WebhookType;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.WebHook;
import com.google.common.annotations.VisibleForTesting;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
// not @Transactional (no DB modifications... yet)

/**
 * Manager that handles post update of translation. Important:
 * TextFlowTargetStateEvent IS NOT asynchronous, that is why
 * DocumentStatisticUpdatedEvent is used for webhook processes. See
 * {@link org.zanata.events.TextFlowTargetStateEvent} See
 * {@link org.zanata.webhook.events.DocumentStatsEvent}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("translationUpdatedManager")
@RequestScoped
public class TranslationUpdatedManager {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationUpdatedManager.class);

    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private DocumentDAO documentDAO;
    @Inject
    private WebhookServiceImpl webhookServiceImpl;

    @Async
    public void docStatsUpdated(@Observes(
            during = TransactionPhase.AFTER_SUCCESS) DocStatsEvent event) {
        processWebHookEvent(event);
    }

    @VisibleForTesting
    protected void processWebHookEvent(DocStatsEvent event) {
        HTextFlowTarget target =
                textFlowTargetDAO.findById(event.getLastModifiedTargetId());
        HPerson person = target.getLastModifiedBy();
        if (person == null) {
            return;
        }
        HDocument document =
                documentDAO.findById(event.getKey().getDocumentId());
        HProject project = document.getProjectIteration().getProject();
        if (project.getWebHooks().isEmpty()) {
            return;
        }
        List<WebHook> docStatsWebHooks = project.getWebHooks().stream()
                .filter(webHook -> webHook.getTypes()
                        .contains(WebhookType.DocumentStatsEvent))
                .collect(Collectors.toList());
        if (docStatsWebHooks.isEmpty()) {
            return;
        }
        String docId = document.getDocId();
        String versionSlug = document.getProjectIteration().getSlug();
        String projectSlug = project.getSlug();
        LocaleId localeId = event.getKey().getLocaleId();
        webhookServiceImpl.processDocumentStats(
                person.getAccount().getUsername(), projectSlug, versionSlug,
                docId, localeId, event.getWordDeltasByState(),
                docStatsWebHooks);
    }

    @VisibleForTesting
    protected void init(DocumentDAO documentDAO,
            TextFlowTargetDAO textFlowTargetDAO,
            WebhookServiceImpl webhookService) {
        this.documentDAO = documentDAO;
        this.textFlowTargetDAO = textFlowTargetDAO;
        this.webhookServiceImpl = webhookService;
    }
}

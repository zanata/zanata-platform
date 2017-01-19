package org.zanata.webtrans.server;

import static org.zanata.transaction.TransactionUtilImpl.runInTransaction;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.persister.entity.EntityPersister;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.events.TextFlowTargetUpdatedEvent;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import javax.enterprise.event.Event;
import org.zanata.servlet.HttpRequestAndSessionHolder;
import org.zanata.util.IServiceLocator;
import org.zanata.webtrans.server.rpc.TransUnitTransformer;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javax.servlet.http.HttpSession;

/**
 * Entity event listener for HTextFlowTarget.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("translationUpdateListener")
@javax.enterprise.context.ApplicationScoped
public class TranslationUpdateListener
        implements PostUpdateEventListener, PostInsertEventListener {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TranslationUpdateListener.class);

    private static final long serialVersionUID = 1L;
    private static final Cache<CacheKey, CacheValue> updateContext =
            CacheBuilder.newBuilder().softValues()
                    .expireAfterAccess(1, TimeUnit.SECONDS).maximumSize(1000)
                    .build();
    @Inject
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Inject
    private IServiceLocator serviceLocator;
    @Inject
    private Event<TextFlowTargetUpdatedEvent> textFlowTargetUpdatedEvent;

    /**
     * Event raised by Text flow target update initiator containing update
     * context.
     *
     * @param event
     */
    public static void
            updateContext(@Observes TextFlowTargetUpdateContextEvent event) {
        updateContext.put(
                new CacheKey(event.getTransUnitId(), event.getLocaleId()),
                new CacheValue(event.getEditorClientId(),
                        event.getUpdateType()));
    }

    @Override
    public void onPostUpdate(final PostUpdateEvent event) {
        Object entity = event.getEntity();
        if (!(entity instanceof HTextFlowTarget)) {
            return;
        }
        final HTextFlowTarget target =
                HTextFlowTarget.class.cast(event.getEntity());
        try {
            runInTransaction(() -> {
                ContentState oldContentState = (ContentState) Iterables.find(
                        Lists.newArrayList(event.getOldState()),
                        Predicates.instanceOf(ContentState.class));
                prepareTransUnitUpdatedEvent(target.getVersionNum() - 1,
                        oldContentState, target);
            });
        } catch (Exception e) {
            log.error("fail to publish TransUnitUpdate event", e);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        // TODO um, no?
        return false;
    }

    private void prepareTransUnitUpdatedEvent(int previousVersionNum,
            ContentState previousState, HTextFlowTarget target) {
        LocaleId localeId = target.getLocaleId();
        HTextFlow textFlow = target.getTextFlow();
        HDocument document = textFlow.getDocument();
        HProjectIteration projectIteration = document.getProjectIteration();
        String iterationSlug = projectIteration.getSlug();
        String projectSlug = projectIteration.getProject().getSlug();
        ProjectType projectType = projectIteration.getProjectType();
        WorkspaceId workspaceId = new WorkspaceId(
                new ProjectIterationId(projectSlug, iterationSlug, projectType),
                localeId);
        Optional<TranslationWorkspace> workspaceOptional =
                translationWorkspaceManager.tryGetWorkspace(workspaceId);
        if (!workspaceOptional.isPresent()) {
            return;
        }
        TransUnitTransformer transUnitTransformer =
                serviceLocator.getInstance(TransUnitTransformer.class);
        TransUnit transUnit = transUnitTransformer.transform(textFlow, target,
                target.getLocale());
        DocumentId documentId =
                new DocumentId(document.getId(), document.getDocId());
        int wordCount = textFlow.getWordCount().intValue();
        TransUnitUpdateInfo updateInfo = createTransUnitUpdateInfo(transUnit,
                documentId, wordCount, previousVersionNum, previousState);
        CacheValue context = updateContext.getIfPresent(
                new CacheKey(transUnit.getId(), transUnit.getLocaleId()));
        TransUnitUpdated updated;
        java.util.Optional<HttpSession> sessionOpt =
                HttpRequestAndSessionHolder.getHttpSession(false);
        if (context != null) {
            updated = new TransUnitUpdated(updateInfo, context.editorClientId,
                    context.updateType);
            log.debug("about to publish trans unit updated event {}", updated);
        } else if (sessionOpt.isPresent()) {
            String sessionId = sessionOpt.get().getId();
            EditorClientId editorClientId = new EditorClientId(sessionId, -1);
            updated = new TransUnitUpdated(updateInfo, editorClientId,
                    TransUnitUpdated.UpdateType.NonEditorSave);
        } else {
            updated = new TransUnitUpdated(updateInfo,
                    new EditorClientId("unknown", -1),
                    TransUnitUpdated.UpdateType.NonEditorSave);
        }
        textFlowTargetUpdatedEvent.fire(new TextFlowTargetUpdatedEvent(
                workspaceOptional.get(), target.getId(), updated));
    }

    private static TransUnitUpdateInfo createTransUnitUpdateInfo(
            TransUnit transUnit, DocumentId documentId, int wordCount,
            int previousVersionNum, ContentState previousState) {
        return new TransUnitUpdateInfo(true, true, documentId, transUnit,
                wordCount, previousVersionNum, previousState, null);
    }

    @Override
    public void onPostInsert(final PostInsertEvent event) {
        Object entity = event.getEntity();
        if (!(entity instanceof HTextFlowTarget)) {
            return;
        }
        final HTextFlowTarget target =
                HTextFlowTarget.class.cast(event.getEntity());
        try {
            runInTransaction(() -> prepareTransUnitUpdatedEvent(0,
                    ContentState.New, target));
        } catch (Exception e) {
            log.error("fail to publish TransUnitUpdate event", e);
        }
    }

    private static class CacheKey {
        private final TransUnitId transUnitId;
        private final LocaleId localeId;

        @java.beans.ConstructorProperties({ "transUnitId", "localeId" })
        public CacheKey(final TransUnitId transUnitId,
                final LocaleId localeId) {
            this.transUnitId = transUnitId;
            this.localeId = localeId;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof TranslationUpdateListener.CacheKey))
                return false;
            final CacheKey other = (CacheKey) o;
            if (!other.canEqual((Object) this))
                return false;
            final Object this$transUnitId = this.transUnitId;
            final Object other$transUnitId = other.transUnitId;
            if (this$transUnitId == null ? other$transUnitId != null
                    : !this$transUnitId.equals(other$transUnitId))
                return false;
            final Object this$localeId = this.localeId;
            final Object other$localeId = other.localeId;
            if (this$localeId == null ? other$localeId != null
                    : !this$localeId.equals(other$localeId))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof TranslationUpdateListener.CacheKey;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $transUnitId = this.transUnitId;
            result = result * PRIME
                    + ($transUnitId == null ? 43 : $transUnitId.hashCode());
            final Object $localeId = this.localeId;
            result = result * PRIME
                    + ($localeId == null ? 43 : $localeId.hashCode());
            return result;
        }
    }

    private static class CacheValue {
        private final EditorClientId editorClientId;
        private final TransUnitUpdated.UpdateType updateType;

        @java.beans.ConstructorProperties({ "editorClientId", "updateType" })
        public CacheValue(final EditorClientId editorClientId,
                final TransUnitUpdated.UpdateType updateType) {
            this.editorClientId = editorClientId;
            this.updateType = updateType;
        }
    }
}

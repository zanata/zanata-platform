package org.zanata.webtrans.server;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.SessionContext;
import org.jboss.seam.util.Work;
import org.jboss.seam.web.ServletContexts;
import org.jboss.seam.web.Session;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.events.TextFlowTargetUpdateContextEvent;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
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
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("translationUpdateListener")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Slf4j
public class TranslationUpdateListener implements PostUpdateEventListener,
        PostInsertEventListener {

    private static final long serialVersionUID = 1L;
    private static final Cache<CacheKey, CacheValue> updateContext =
            CacheBuilder.newBuilder().softValues()
                    .expireAfterAccess(1, TimeUnit.SECONDS).build();

    @In(create = true)
    private TranslationWorkspaceManager translationWorkspaceManager;

    @In
    private TransUnitTransformer transUnitTransformer;

    @Observer(TextFlowTargetUpdateContextEvent.EVENT_NAME)
    public static void updateContext(TextFlowTargetUpdateContextEvent event) {
        updateContext
                .put(new CacheKey(event.getTransUnitId(), event.getLocaleId()),
                        new CacheValue(event.getEditorClientId(), event
                                .getUpdateType()));
    }

    @Override
    public void onPostUpdate(final PostUpdateEvent event) {
        Object entity = event.getEntity();
        if (!(entity instanceof HTextFlowTarget)) {
            return;
        }

        try {
            new Work<Void>() {
                @Override
                protected Void work() throws Exception {
                    ContentState oldContentState =
                            (ContentState) Iterables.find(
                                    Lists.newArrayList(event.getOldState()),
                                    Predicates.instanceOf(ContentState.class));

                    HTextFlowTarget target =
                            HTextFlowTarget.class.cast(event.getEntity());
                    publishTransUnitUpdatedEvent(target.getVersionNum() - 1,
                            oldContentState, target);
                    return null;
                }
            }.workInTransaction();
        } catch (Exception e) {
            log.error("fail to publish TransUnitUpdate event", e);
        }

    }

    private void publishTransUnitUpdatedEvent(int previousVersionNum,
            ContentState previousState, HTextFlowTarget target) {
        LocaleId localeId = target.getLocaleId();
        HTextFlow textFlow = target.getTextFlow();
        HDocument document = textFlow.getDocument();
        HProjectIteration projectIteration = document.getProjectIteration();
        String iterationSlug = projectIteration.getSlug();
        String projectSlug = projectIteration.getProject().getSlug();
        ProjectType projectType = projectIteration.getProjectType();

        WorkspaceId workspaceId =
                new WorkspaceId(new ProjectIterationId(projectSlug,
                        iterationSlug, projectType), localeId);
        Optional<TranslationWorkspace> workspaceOptional =
                translationWorkspaceManager.tryGetWorkspace(workspaceId);
        if (!workspaceOptional.isPresent()) {
            return;
        }

        TransUnit transUnit =
                transUnitTransformer.transform(textFlow, target,
                        target.getLocale());

        DocumentId documentId =
                new DocumentId(document.getId(), document.getDocId());
        int wordCount = textFlow.getWordCount().intValue();

        TransUnitUpdateInfo updateInfo =
                createTransUnitUpdateInfo(transUnit, documentId, wordCount,
                        previousVersionNum, previousState);

        CacheValue context =
                updateContext.getIfPresent(new CacheKey(transUnit.getId(),
                        transUnit.getLocaleId()));
        TransUnitUpdated updated;
        if (context != null) {
            EditorClientId editorClientId = context.editorClientId;
            TransUnitUpdated.UpdateType updateType = context.updateType;
            updated =
                    new TransUnitUpdated(updateInfo, editorClientId, updateType);
            log.debug("about to publish trans unit updated event {}", updated);
        } else if (ServletContexts.instance().getRequest() != null) {

            String sessionId = ServletContexts.instance().getRequest().getSession()
                    .getId();
            EditorClientId editorClientId = new EditorClientId(sessionId, -1);
            updated = new TransUnitUpdated(updateInfo, editorClientId,
                            TransUnitUpdated.UpdateType.NonEditorSave);
        } else {
            updated = new TransUnitUpdated(updateInfo, new EditorClientId("unknown", -1),
                    TransUnitUpdated.UpdateType.NonEditorSave);
        }
        workspaceOptional.get().publish(updated);
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
        try {
            new Work<Void>() {

                @Override
                protected Void work() throws Exception {
                    HTextFlowTarget target =
                            HTextFlowTarget.class.cast(event.getEntity());
                    publishTransUnitUpdatedEvent(0, ContentState.New, target);
                    return null;
                }
            }.workInTransaction();
        } catch (Exception e) {
            log.error("fail to publish TransUnitUpdate event", e);
        }

    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class CacheKey {
        private final TransUnitId transUnitId;
        private final LocaleId localeId;
    }

    @RequiredArgsConstructor
    private static class CacheValue {
        private final EditorClientId editorClientId;
        private final TransUnitUpdated.UpdateType updateType;
    }
}

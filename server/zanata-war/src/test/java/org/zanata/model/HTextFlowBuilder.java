package org.zanata.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.zanata.common.ContentState;
import org.zanata.model.po.HPotEntryData;

import java.util.Date;

/**
 * A builder to build HTextFlow entity and possibly [HTextFlowTarget] [and
 * HSimpleComment for source and/or target] [and HPotEntryData for msgCtxt].
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class HTextFlowBuilder {
    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(HTextFlowBuilder.class);
    private HDocument document;
    private HLocale targetLocale;

    // source
    private String resId;
    private String sourceContent;
    private String sourceComment;
    private String msgContext;

    // target
    private String targetContent;
    private ContentState targetState;
    private HPerson lastModifiedBy;
    private DateTime lastModifiedDate;
    private String targetComment;

    @java.beans.ConstructorProperties({ "document", "targetLocale", "resId",
            "sourceContent", "sourceComment", "msgContext", "targetContent",
            "targetState", "lastModifiedBy", "lastModifiedDate",
            "targetComment" })
    private HTextFlowBuilder(HDocument document, HLocale targetLocale,
            String resId, String sourceContent, String sourceComment,
            String msgContext, String targetContent, ContentState targetState,
            HPerson lastModifiedBy, DateTime lastModifiedDate,
            String targetComment) {
        this.document = document;
        this.targetLocale = targetLocale;
        this.resId = resId;
        this.sourceContent = sourceContent;
        this.sourceComment = sourceComment;
        this.msgContext = msgContext;
        this.targetContent = targetContent;
        this.targetState = targetState;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.targetComment = targetComment;
    }

    public HTextFlowBuilder() {
    }

    public HTextFlow build() {
        Preconditions.checkNotNull(resId, "resId can not be null");
        Preconditions.checkNotNull(sourceContent,
                "sourceContent can not be null");

        HTextFlow hTextFlow = new HTextFlow(document, resId, sourceContent);
        document.getTextFlows().add(hTextFlow);
        if (msgContext != null) {
            HPotEntryData potEntryData = new HPotEntryData();
            potEntryData.setContext(msgContext);
            hTextFlow.setPotEntryData(potEntryData);
        }
        if (sourceComment != null) {
            hTextFlow.setComment(new HSimpleComment(sourceComment));
        }
        if (targetContent != null) {
            HTextFlowTarget target =
                    new HTextFlowTarget(hTextFlow, targetLocale);
            hTextFlow.getTargets().put(targetLocale.getId(), target);
            target.setContents(targetContent);
            target.setLastModifiedBy(lastModifiedBy);
            if (targetState == ContentState.Approved || targetState == ContentState.Approved) {
                target.setReviewer(lastModifiedBy);
            } else {
                target.setTranslator(lastModifiedBy);
            }
            if (lastModifiedDate != null) {
                target.setLastChanged(lastModifiedDate.toDate());
            } else {
                target.setLastChanged(new Date());
            }
            target.setState(targetState);
            if (targetComment != null) {
                target.setComment(new HSimpleComment(targetComment));
            }
        }
        log.debug(this.toString());
        return hTextFlow;
    }

    @Override
    public String toString() {
        // @formatter:off
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("resId", resId)
                .add("sourceContent", sourceContent)
                .add("sourceComment", sourceComment)
                .add("msgContext", msgContext)
                .add("targetContent", targetContent)
                .add("targetState", targetState)
                .add("lastModifiedBy", lastModifiedBy)
                .add("lastModifiedDate", lastModifiedDate)
                .add("targetComment", targetComment)
                .toString();
        // @formatter:on
    }

    public HTextFlowBuilder withDocument(HDocument document) {
        return this.document == document ? this :
                new HTextFlowBuilder(document, this.targetLocale, this.resId,
                        this.sourceContent, this.sourceComment, this.msgContext,
                        this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withTargetLocale(HLocale targetLocale) {
        return this.targetLocale == targetLocale ? this :
                new HTextFlowBuilder(this.document, targetLocale, this.resId,
                        this.sourceContent, this.sourceComment, this.msgContext,
                        this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withResId(String resId) {
        return this.resId == resId ? this :
                new HTextFlowBuilder(this.document, this.targetLocale, resId,
                        this.sourceContent, this.sourceComment, this.msgContext,
                        this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withSourceContent(String sourceContent) {
        return this.sourceContent == sourceContent ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, sourceContent, this.sourceComment,
                        this.msgContext, this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withSourceComment(String sourceComment) {
        return this.sourceComment == sourceComment ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, sourceComment,
                        this.msgContext, this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withMsgContext(String msgContext) {
        return this.msgContext == msgContext ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, this.sourceComment,
                        msgContext, this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withTargetContent(String targetContent) {
        return this.targetContent == targetContent ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, this.sourceComment,
                        this.msgContext, targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withTargetState(ContentState targetState) {
        return this.targetState == targetState ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, this.sourceComment,
                        this.msgContext, this.targetContent, targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withLastModifiedBy(HPerson lastModifiedBy) {
        return this.lastModifiedBy == lastModifiedBy ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, this.sourceComment,
                        this.msgContext, this.targetContent, this.targetState,
                        lastModifiedBy, this.lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withLastModifiedDate(DateTime lastModifiedDate) {
        return this.lastModifiedDate == lastModifiedDate ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, this.sourceComment,
                        this.msgContext, this.targetContent, this.targetState,
                        this.lastModifiedBy, lastModifiedDate,
                        this.targetComment);
    }

    public HTextFlowBuilder withTargetComment(String targetComment) {
        return this.targetComment == targetComment ? this :
                new HTextFlowBuilder(this.document, this.targetLocale,
                        this.resId, this.sourceContent, this.sourceComment,
                        this.msgContext, this.targetContent, this.targetState,
                        this.lastModifiedBy, this.lastModifiedDate,
                        targetComment);
    }
}

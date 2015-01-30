package org.zanata.model;

import java.util.Date;

import org.joda.time.DateTime;
import org.zanata.common.ContentState;
import org.zanata.model.po.HPotEntryData;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

/**
 * A builder to build HTextFlow entity and possibly [HTextFlowTarget] [and
 * HSimpleComment for source and/or target] [and HPotEntryData for msgCtxt].
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Wither
@Slf4j
public class HTextFlowBuilder {
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
        return Objects.toStringHelper(this).omitNullValues()
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
}

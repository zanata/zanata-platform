package org.zanata.rest.editor.service;

import java.io.Serializable;
import java.util.List;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.po.HPotEntryData;
import org.zanata.rest.editor.dto.EditorTextFlow;
import org.zanata.rest.editor.dto.EditorTextFlowTarget;
import org.zanata.rest.editor.dto.TransUnit;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.service.ResourceUtils;
import com.google.common.collect.Lists;
import org.zanata.webtrans.server.rpc.TransUnitTransformer;

import javax.annotation.Nonnull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("transUnitUtils")
@javax.enterprise.context.Dependent
public class TransUnitUtils implements Serializable {
    private static final long serialVersionUID = 5128782948971430463L;
    public static final int MAX_SIZE = 200;
    public static final String ID_SEPARATOR = ",";
    @Inject
    private ResourceUtils resourceUtils;

    /**
     * Filter out non-numeric id and convert from String to Long.
     *
     * @param idsString
     */
    public static List<Long> filterAndConvertIdsToList(String idsString) {
        List<String> ids = Lists.newArrayList(idsString.split(ID_SEPARATOR));
        List<Long> idList = Lists.newArrayList();
        for (String id : ids) {
            if (StringUtils.isNumeric(id)) {
                idList.add(Long.parseLong(id));
            }
        }
        return idList;
    }

    /**
     * Build a fully-populated TransUnit data object ready for serialization.
     */
    public TransUnit buildTransUnitFull(@Nonnull HTextFlow hTf,
            HTextFlowTarget hTft, LocaleId localeId) {
        TransUnit tu = new TransUnit();
        tu.putAll(buildSourceTransUnit(hTf, localeId));
        tu.putAll(buildTargetTransUnit(hTf, hTft, localeId));
        return tu;
    }

    public TransUnit buildSourceTransUnit(HTextFlow hTf, LocaleId localeId) {
        TransUnit tu = new TransUnit();
        EditorTextFlow tf = new EditorTextFlow(hTf.getResId(), localeId);
        transferToTextFlow(hTf, tf);
        tu.put(TransUnit.SOURCE, tf);
        return tu;
    }

    public TransUnit buildTargetTransUnit(HTextFlow hTf, HTextFlowTarget hTft,
            LocaleId localeId) {
        TransUnit tu = new TransUnit();
        if (hTft != null) {
            EditorTextFlowTarget target = new EditorTextFlowTarget(hTf.getResId());
            resourceUtils.transferToTextFlowTarget(hTft, target,
                    Optional.of("Editor"));
            target.setLastModifiedTime(hTft.getLastChanged());
            tu.put(hTft.getLocaleId().toString(), target);
        } else {
            tu.put(localeId.toString(), new TextFlowTarget(hTf.getResId()));
        }
        return tu;
    }

    public void transferToTextFlow(HTextFlow from, EditorTextFlow to) {
        resourceUtils.transferToTextFlow(from, to);
        to.setWordCount(from.getWordCount().intValue());

        HPotEntryData potEntryData = from.getPotEntryData();

        if (potEntryData != null) {
            to.setMsgctxt(potEntryData.getContext());
            to.setSourceReferences(potEntryData.getReferences());
            to.setsourceFlags(potEntryData.getFlags());
        }

        to.setSourceComment(
                TransUnitTransformer.commentToString(from.getComment()));
    }

    public TransUnitUtils() {
    }

    @java.beans.ConstructorProperties({ "resourceUtils" })
    protected TransUnitUtils(final ResourceUtils resourceUtils) {
        this.resourceUtils = resourceUtils;
    }
}

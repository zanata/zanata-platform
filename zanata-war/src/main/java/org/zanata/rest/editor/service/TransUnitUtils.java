package org.zanata.rest.editor.service;

import java.util.List;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.editor.dto.EditorTextFlow;
import org.zanata.rest.editor.dto.TransUnit;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.service.ResourceUtils;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("transUnitUtils")
@Scope(ScopeType.STATELESS)
@Slf4j
@AutoCreate
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TransUnitUtils {
    public static int MAX_SIZE = 200;
    public static String ID_SEPARATOR = ",";

    @In
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

    public TransUnit buildTransUnitFull(@Nonnull HTextFlow hTf,
            HTextFlowTarget hTft, LocaleId localeId) {

        TransUnit tu = new TransUnit();

        // build source
        tu.putAll(buildSourceTransUnit(hTf, localeId));

        // build target
        tu.putAll(buildTargetTransUnit(hTf, hTft, localeId));

        return tu;
    }

    public TransUnit buildSourceTransUnit(HTextFlow hTf, LocaleId localeId) {
        TransUnit tu = new TransUnit();
        EditorTextFlow tf =
                new EditorTextFlow(hTf.getResId(), localeId);
        transferToTextFlow(hTf, tf);
        tu.put(TransUnit.SOURCE, tf);
        return tu;
    }

    public TransUnit buildTargetTransUnit(HTextFlow hTf, HTextFlowTarget hTft,
            LocaleId localeId) {
        TransUnit tu = new TransUnit();

        if (hTft != null) {
            TextFlowTarget target = new TextFlowTarget(hTf.getResId());
            resourceUtils.transferToTextFlowTarget(hTft, target,
                    Optional.of("Editor"));
            tu.put(hTft.getLocaleId().toString(), target);
        } else {
            tu.put(localeId.toString(),
                    new TextFlowTarget(hTf.getResId()));
        }
        return tu;
    }

    public void transferToTextFlow(HTextFlow from, EditorTextFlow to) {
        resourceUtils.transferToTextFlow(from, to);
        to.setWordCount(from.getWordCount().intValue());
    }
}

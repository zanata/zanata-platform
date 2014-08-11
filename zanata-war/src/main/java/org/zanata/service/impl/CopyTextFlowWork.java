package org.zanata.service.impl;

import java.util.List;
import java.util.Map;

import org.jboss.seam.util.Work;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlow;
import org.zanata.service.CopyVersionService;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Run copy text flow and persist in transaction.
 *
 * Copy HTextFlow from HDocument(id=documentId) in batches(batchStart,
 * batchLength) into HDocument(id=newDocumentId).
 *
 * @see CopyVersionService#copyTextFlow
 *
 * @return Map of original HTextFlow id => copied HTextFlow id
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Slf4j
@AllArgsConstructor
public class CopyTextFlowWork extends Work<Map<Long, Long>> {

    private final Long documentId;
    private final Long newDocumentId;
    private final TextFlowDAO textFlowDAO;
    private final DocumentDAO documentDAO;
    private final CopyVersionService copyVersionService;
    private final int batchStart;
    private final int batchLength;

    private static final Maps.EntryTransformer<Long, HTextFlow, Long> transformer =
            new Maps.EntryTransformer<Long, HTextFlow, Long>() {
                @Override
                public Long transformEntry(Long key, HTextFlow value) {
                    return value.getId();
                }
            };

    @Override
    protected Map<Long, Long> work() throws Exception {
        Map<Long, HTextFlow> tfMap = Maps.newHashMap();

        List<HTextFlow> textFlows = textFlowDAO.getTextFlowsByDocumentId(
                documentId, batchStart, batchLength);

        HDocument newDocument = documentDAO.getById(newDocumentId);
        for (HTextFlow textFlow : textFlows) {
            HTextFlow newTextFlow =
                    copyVersionService.copyTextFlow(newDocument,
                            textFlow);

            newDocument.getTextFlows().add(newTextFlow);
            newDocument.getAllTextFlows()
                    .put(newTextFlow.getResId(), newTextFlow);
            tfMap.put(textFlow.getId(), newTextFlow);
        }
        documentDAO.makePersistent(newDocument);
        documentDAO.flush();

        return Maps.transformEntries(tfMap, transformer);
    }
}

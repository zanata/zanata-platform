package org.zanata.webtrans.shared.rpc;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;

public class GetDocumentListResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private ProjectIterationId projectIterationId;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private List<DocumentInfo> documents;

    @SuppressWarnings("unused")
    private GetDocumentListResult() {
    }

    public GetDocumentListResult(ProjectIterationId projectIterationId,
            List<DocumentInfo> documents) {
        this.projectIterationId = projectIterationId;
        this.documents = documents;
    }

    public List<DocumentInfo> getDocuments() {
        return documents;
    }

    public ProjectIterationId getProjectIterationId() {
        return projectIterationId;
    }
}

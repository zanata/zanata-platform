package org.zanata.webtrans.server.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.test.EntityTestData.setId;

import java.util.Arrays;
import java.util.List;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.file.FilePersistService;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationStateCache;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;
import org.zanata.webtrans.test.GWTTestData;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class GetDocumentListHandlerTest extends ZanataTest {
    @Inject @Any
    private GetDocumentListHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private DocumentDAO documentDAO;
    @Produces @Mock
    private TranslationFileService translationFileServiceImpl;
    @Produces @Mock
    private TranslationStateCache translationStateCacheImpl;
    @Produces @Mock
    private FilePersistService filePersistService;

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        GetDocumentList action = new GetDocumentList();
        action.setWorkspaceId(workspaceId);
        HDocument hDocument = hDocument(1);
        List<HDocument> documentList = Arrays.asList(hDocument);

        when(documentDAO.getAllByProjectIteration("project", "master"))
                .thenReturn(documentList);

        GetDocumentListResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getDocuments()).hasSize(1);
        DocumentInfo documentInfo = result.getDocuments().get(0);
        assertThat(documentInfo.getId()).isEqualTo(new DocumentId(new Long(1), ""));
        assertThat(documentInfo.getPath()).isEqualTo("/dot/");
        assertThat(documentInfo.getName()).isEqualTo("a.po");
    }

    @Test
    @InRequestScope
    public void testExecuteWithFilter() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        GetDocumentList action = new GetDocumentList();
        action.setWorkspaceId(workspaceId);
        HDocument hDocument = hDocument(1);
        List<HDocument> documentList = Arrays.asList(hDocument);
        when(documentDAO.getAllByProjectIteration("project", "master"))
                .thenReturn(documentList);

        GetDocumentListResult result = handler.execute(action, null);

        assertThat(result.getDocuments()).hasSize(1);
    }

    private HDocument hDocument(long id) {
        HProjectIteration iteration = new HProjectIteration();
        iteration.setProjectType(ProjectType.Podir);

        HDocument hDocument =
                new HDocument("/dot/a.po", ContentType.PO, new HLocale(
                        LocaleId.EN_US));
        hDocument.setProjectIteration(iteration);
        setId(hDocument, id);
        return hDocument;
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}

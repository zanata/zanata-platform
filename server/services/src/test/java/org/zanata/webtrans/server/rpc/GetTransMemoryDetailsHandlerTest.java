package org.zanata.webtrans.server.rpc;

import java.util.Date;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.jpa.FullText;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.TranslationMemoryServiceImpl;
import org.zanata.test.CdiUnitRunner;
import org.zanata.test.EntityTestData;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;
import org.zanata.webtrans.test.GWTTestData;
import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.test.EntityTestData.makeApprovedHTextFlow;
import static org.zanata.test.EntityTestData.setId;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        TranslationMemoryServiceImpl.class
})
public class GetTransMemoryDetailsHandlerTest extends ZanataTest {
    @Inject @Any
    private GetTransMemoryDetailsHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private TextFlowDAO textFlowDAO;
    @Produces @Mock
    private LocaleService localeServiceImpl;
    @Produces @Mock
    private UrlUtil urlUtil;
    @Produces @Mock @FullText
    private FullTextEntityManager fullTextEntityManager;

    private HLocale hLocale;

    @Before
    public void setUp() throws Exception {
        hLocale = new HLocale(LocaleId.EN);
        setId(hLocale, 1L);
    }

    private static void setProjectAndIterationSlug(HTextFlow hTextFlow,
            String projectSlug, String iterationSlug) {
        HProjectIteration projectIteration = new HProjectIteration();
        projectIteration.setSlug(iterationSlug);
        HProject project = new HProject();
        project.setName(projectSlug);
        projectIteration.setProject(project);
        hTextFlow.getDocument().setProjectIteration(projectIteration);
    }

    private static HTextFlowTarget addHTextFlowTarget(HLocale hLocale,
            HTextFlow hTextFlow, String username) {
        HTextFlowTarget hTextFlowTarget =
                new HTextFlowTarget(hTextFlow, hLocale);
        HPerson lastModifiedBy = new HPerson();
        HAccount account = new HAccount();
        account.setUsername(username);
        lastModifiedBy.setAccount(account);
        hTextFlowTarget.setLastModifiedBy(lastModifiedBy);
        hTextFlowTarget.setLastChanged(new Date());
        return hTextFlowTarget;
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        WorkspaceId workspaceId =
                GWTTestData.workspaceId(hLocale.getLocaleId());
        GetTransMemoryDetailsAction action =
                new GetTransMemoryDetailsAction(1L, 2L);
        action.setWorkspaceId(workspaceId);
        ProjectIterationId projectIterationId =
                workspaceId.getProjectIterationId();
        when(
                localeServiceImpl.validateLocaleByProjectIteration(
                        workspaceId.getLocaleId(),
                        projectIterationId.getProjectSlug(),
                        projectIterationId.getIterationSlug())).thenReturn(
                hLocale);
        // text flow 1
        HTextFlow hTextFlow1 = makeApprovedHTextFlow(1L, hLocale);
        setProjectAndIterationSlug(hTextFlow1, "project", "master");
        addHTextFlowTarget(hLocale, hTextFlow1, null);
        // text flow 2
        HTextFlow hTextFlow2 = makeApprovedHTextFlow(2L, hLocale);
        setProjectAndIterationSlug(hTextFlow2, "project 2", "beta");
        addHTextFlowTarget(hLocale, hTextFlow2, null);
        when(textFlowDAO.findByIdList(Lists.newArrayList(1L, 2L))).thenReturn(
                Lists.newArrayList(hTextFlow1, hTextFlow2));

        TransMemoryDetailsList result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getItems()).hasSize(2);
    }

    @Test(expected = ActionException.class)
    @InRequestScope
    public void testExecuteWithInvalidLocale() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        GetTransMemoryDetailsAction action = new GetTransMemoryDetailsAction();
        action.setWorkspaceId(workspaceId);
        ProjectIterationId projectIterationId =
                workspaceId.getProjectIterationId();
        when(
                localeServiceImpl.validateLocaleByProjectIteration(
                        workspaceId.getLocaleId(),
                        projectIterationId.getProjectSlug(),
                        projectIterationId.getIterationSlug())).thenThrow(
                new ZanataServiceException("test"));

        handler.execute(action, null);
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}

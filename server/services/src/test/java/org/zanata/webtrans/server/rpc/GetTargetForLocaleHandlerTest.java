package org.zanata.webtrans.server.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.dbunit.operation.DatabaseOperation;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.IdForLocale;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTargetForLocale;
import org.zanata.webtrans.shared.rpc.GetTargetForLocaleResult;
import org.zanata.webtrans.test.GWTTestData;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@RunWith(CdiUnitRunner.class)
public class GetTargetForLocaleHandlerTest extends ZanataDbunitJpaTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(GetTargetForLocaleHandlerTest.class);

    @Inject
    @Any
    private GetTargetForLocaleHandler handler;
    @Produces
    @Mock
    private ZanataIdentity identity;
    @Produces
    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;
    private GetTargetForLocale action;
    private final LocaleId localeId = new LocaleId("ja");
    private HLocale jaHLocale;
    private TransUnitId sourceTransUnitId;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new ZanataDbunitJpaTest.DataSetOperation(
                "performance/GetTransUnitListTest.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setUp() throws Exception {
        ResourceUtils resourceUtils = new ResourceUtils();
        resourceUtils.create(); // postConstruct
        jaHLocale = getEm().find(HLocale.class, 3L);
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        // plural="true" content0="One file removed_" content1="%d more files
        // removed"
        sourceTransUnitId = new TransUnitId(3L);
        action = new GetTargetForLocale(sourceTransUnitId,
                new Locale(new IdForLocale(jaHLocale.getId(), localeId), ""));
        action.setWorkspaceId(workspaceId);
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        when(textFlowTargetDAO.getTextFlowTarget(sourceTransUnitId.getId(),
                jaHLocale.getLocaleId()))
                        .thenReturn(getEm().find(HTextFlowTarget.class, 61L));
        GetTargetForLocaleResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getTarget().getContent()).isEqualTo(
                getEm().find(HTextFlowTarget.class, 61L).getContents().get(0));
    }

    @Test
    @InRequestScope
    public void testExecuteWhenTargetLangDoesNotExsist() throws Exception {
        when(textFlowTargetDAO.getTextFlowTarget(sourceTransUnitId.getId(),
                jaHLocale.getLocaleId())).thenReturn(null);
        GetTargetForLocaleResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getTarget()).isNull();
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}

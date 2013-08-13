package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import lombok.extern.slf4j.Slf4j;
import org.dbunit.operation.DatabaseOperation;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.model.IdForLocale;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTargetForLocale;
import org.zanata.webtrans.shared.rpc.GetTargetForLocaleResult;

/**
 * @author Patrick Huang <a
 * href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = {"jpa-tests"})
@Slf4j
public class GetTargetForLocaleHandlerTest extends ZanataDbunitJpaTest
{
    private GetTargetForLocaleHandler handler;

    @Mock
    private ZanataIdentity identity;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    private GetTargetForLocale action;

    private final LocaleId localeId = new LocaleId("ja");
    private HLocale jaHLocale;
    private TransUnitId sourceTransUnitId;

    @Override
    protected void prepareDBUnitOperations()
    {
        beforeTestOperations.add(new ZanataDbunitJpaTest.DataSetOperation("performance/GetTransUnitListTest.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
        ResourceUtils resourceUtils = new ResourceUtils();
        resourceUtils.create(); // postConstruct
        TransUnitTransformer transUnitTransformer = SeamAutowire.instance().use("resourceUtils", resourceUtils).autowire(TransUnitTransformer.class);

        handler = SeamAutowire.instance()
                .use("identity", identity)
                .use("textFlowTargetDAO", textFlowTargetDAO)
                .use("transUnitTransformer", transUnitTransformer)
                .ignoreNonResolvable()
                .autowire(GetTargetForLocaleHandler.class);
        // @formatter:on

        jaHLocale = getEm().find(HLocale.class, 3L);

        WorkspaceId workspaceId = TestFixture.workspaceId();

        sourceTransUnitId = new TransUnitId(3L); //plural="true" content0="One file removed_" content1="%d more files removed"/>
        action = new GetTargetForLocale(sourceTransUnitId, new Locale(new IdForLocale(jaHLocale.getId(), localeId), ""));
        action.setWorkspaceId(workspaceId);
    }

    @Test
    public void testExecute() throws Exception
    {        
        when(textFlowTargetDAO.getTextFlowTarget(sourceTransUnitId.getId(), jaHLocale.getLocaleId())).thenReturn(getEm().find(HTextFlowTarget.class, 61L));

        
        GetTargetForLocaleResult result = handler.execute(action, null);
        
        verify(identity).checkLoggedIn();

        assertThat(result.getTarget().getContent(), Matchers.equalTo(getEm().find(HTextFlowTarget.class, 61L).getContents().get(0)));
    }
    
    @Test
    public void testExecuteWhenTargetLangDoesNotExsist() throws Exception
    {        
        when(textFlowTargetDAO.getTextFlowTarget(sourceTransUnitId.getId(), jaHLocale.getLocaleId())).thenReturn(null);

        
        GetTargetForLocaleResult result = handler.execute(action, null);
        
        verify(identity).checkLoggedIn();

        assertThat(result.getTarget(), Matchers.equalTo(null));
    }

    @Test
    public void testRollback() throws Exception
    {
        handler.rollback(null, null, null);
    }
}

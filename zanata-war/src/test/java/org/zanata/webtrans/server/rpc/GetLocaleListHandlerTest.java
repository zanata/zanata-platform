package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.HashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetLocaleList;
import org.zanata.webtrans.shared.rpc.GetLocaleListResult;

/**
 * @author Patrick Huang <a
 * href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = {"jpa-tests"})
@Slf4j
public class GetLocaleListHandlerTest
{
    private GetLocaleListHandler handler;

    @Mock
    private ZanataIdentity identity;

    @Mock
    private LocaleDAO localeDAO;

    @Mock
    private ProjectIterationDAO projectIterationDAO;

    @Mock
    private ProjectDAO projectDAO;
    
    @Mock
    private HProjectIteration hProjectIteration;
    
    @Mock
    private HProject hProject;
    
    private GetLocaleList action;

    @BeforeMethod
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
        handler = SeamAutowire.instance()
                .use("identity", identity)
                .use("projectIterationDAO", projectIterationDAO)
                .use("projectDAO", projectDAO)
                .use("localeDAO", localeDAO)
                .ignoreNonResolvable()
                .autowire(GetLocaleListHandler.class);
        // @formatter:on
        
        WorkspaceId workspaceId = TestFixture.workspaceId();
        action = new GetLocaleList();
        action.setWorkspaceId(workspaceId);
        
        Set<HLocale> iterationLocales = new HashSet<HLocale>();
        iterationLocales.add(new HLocale(LocaleId.EN_US));
        iterationLocales.add(new HLocale(LocaleId.DE));
        when(hProjectIteration.getId()).thenReturn(1L);
        when(hProjectIteration.getCustomizedLocales()).thenReturn(iterationLocales);
        when(projectIterationDAO.getBySlug("project", "master")).thenReturn(hProjectIteration);
        
        Set<HLocale> projectLocales = new HashSet<HLocale>();
        projectLocales.add(new HLocale(LocaleId.ES));
        when(hProject.getId()).thenReturn(1L);
        when(hProject.getCustomizedLocales()).thenReturn(projectLocales);
        when(projectDAO.getBySlug("project")).thenReturn(hProject);
        
        List<HLocale> defaultLocales = new ArrayList<HLocale>();
        defaultLocales.add(new HLocale(LocaleId.FR));
        defaultLocales.add(new HLocale(LocaleId.EN));
        defaultLocales.add(new HLocale(LocaleId.DE));
        when(localeDAO.findAllActiveAndEnabledByDefault()).thenReturn(defaultLocales);
    }

    @Test
    public void testExecute() throws Exception
    {        
        when(hProjectIteration.getOverrideLocales()).thenReturn(false);
        when(hProject.getOverrideLocales()).thenReturn(false);

        GetLocaleListResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getLocales(), Matchers.hasSize(3));
        
        assertThat(result.getLocales().get(0).getId().getLocaleId(), Matchers.equalTo(LocaleId.FR));
        assertThat(result.getLocales().get(1).getId().getLocaleId(), Matchers.equalTo(LocaleId.EN));
        assertThat(result.getLocales().get(2).getId().getLocaleId(), Matchers.equalTo(LocaleId.DE));
    }
    
    @Test
    public void testExecuteWithOverriddenProjectLocales() throws Exception
    {
        when(hProject.getOverrideLocales()).thenReturn(true);

        GetLocaleListResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getLocales(), Matchers.hasSize(1));
        assertThat(result.getLocales().get(0).getId().getLocaleId(), Matchers.equalTo(LocaleId.ES));
    }
    
    @Test
    public void testExecuteWithOverriddenIterationLocales() throws Exception
    {
        when(hProject.getOverrideLocales()).thenReturn(true);
        when(hProjectIteration.getOverrideLocales()).thenReturn(true);

        GetLocaleListResult result = handler.execute(action, null);
        verify(identity).checkLoggedIn();
        assertThat(result.getLocales(), Matchers.hasSize(2));
        assertThat(result.getLocales().get(0).getId().getLocaleId(), Matchers.equalTo(LocaleId.DE));
        assertThat(result.getLocales().get(1).getId().getLocaleId(), Matchers.equalTo(LocaleId.EN_US));
    }
    

    @Test
    public void testRollback() throws Exception
    {
        handler.rollback(null, null, null);
    }
}

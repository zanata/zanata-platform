package org.zanata.rest.graphQL;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.seam.security.AltCurrentUser;
import org.zanata.service.GraphQLService;
import org.zanata.service.UserAccountService;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class GraphQLRestServiceTest extends ZanataDbunitJpaTest {

    private GraphQLService graphQLService;

    private GraphQLRestService graphQLRestService;

    private AccountDAO accountDAO;

    @Produces
    private AltCurrentUser currentUser = new AltCurrentUser();

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/ClearAllTables.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/AccountData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setUp() throws Exception {
        accountDAO = new AccountDAO(getSession());
        graphQLService = new GraphQLService(accountDAO);
        graphQLService.onCreate(null);
        currentUser.account = getEm().find(HAccount.class, 1L);
        graphQLRestService = new GraphQLRestService(currentUser, graphQLService);
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Test
    @InRequestScope
    public void exportUserDataTest() {
        Response response = graphQLRestService.exportUserData();
        System.out.println(response.getEntity().toString());
    }
}

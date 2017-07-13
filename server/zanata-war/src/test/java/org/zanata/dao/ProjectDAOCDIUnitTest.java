package org.zanata.dao;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.assertj.core.api.Assertions;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.action.ReindexClassOptions;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.jpa.FullText;
import org.zanata.model.HProject;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.IndexingService;
import org.zanata.service.impl.IndexingServiceImpl;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.Zanata;
import com.google.common.collect.Maps;


@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ IndexingServiceImpl.class})
public class ProjectDAOCDIUnitTest extends ZanataDbunitJpaTest {
    @Produces
    Session produceSession() {
        return getSession();
    }

    @Produces @FullText
    FullTextEntityManager getFulltextEM() {
        return Search.getFullTextEntityManager(getEm());
    }

    @Produces
    @Zanata
    EntityManagerFactory getEntityManagerFactory() {
        return getEmf();
    }

    @Inject
    private ProjectDAO dao;

    @Inject
    private IndexingService indexingService;

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @InRequestScope
    public void canDoFullTextSearch() throws Exception {
        HashMap<Class<?>, ReindexClassOptions> indexingOptions =
                Maps.newHashMap();
        ReindexClassOptions option = new ReindexClassOptions(HProject.class);
        option.setSelectAll(true);
        indexingOptions.put(HProject.class, option);
        Future<Void> future = indexingService
                .startIndexing(indexingOptions, new AsyncTaskHandle<>());
        future.get(500, TimeUnit.SECONDS);

        HProject expected = dao.getBySlug("sample-project");


        List<HProject> result =
                dao.searchProjects("sam", 10, 0, false);
        Assertions.assertThat(result).contains(expected);

        List<HProject> result1 = dao.searchProjects("sampl ", 10, 0, false);
        Assertions.assertThat(result1).contains(expected);

        List<HProject> result2 = dao.searchProjects("sample-", 10, 0, false);
        Assertions.assertThat(result2).contains(expected);

        List<HProject> result3 = dao.searchProjects("a project", 10, 0, false);
        Assertions.assertThat(result3).contains(expected);
    }
}

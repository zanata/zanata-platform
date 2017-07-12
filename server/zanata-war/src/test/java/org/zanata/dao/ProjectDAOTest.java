package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import com.google.common.collect.Maps;

@RunWith(CdiUnitRunner.class)
@AdditionalClasses({ IndexingServiceImpl.class})
public class ProjectDAOTest extends ZanataDbunitJpaTest {
    @Produces Session produceSession() {
        return getSession();
    }

    @Produces @FullText FullTextEntityManager getFulltextEM() {
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

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Test
    public void getValidProjectBySlug() {
        HProject project = dao.getBySlug("sample-project");
        assertThat(project, notNullValue());
        assertThat(project.getName(), is("Sample Project"));
    }

    @Test
    public void getValidProjectById() {
        HProject project = dao.findById(1l, false);
        assertThat(project, notNullValue());
        assertThat(project.getName(), is("Sample Project"));
    }

    @Test
    public void getActiveIterations() {
        assertThat(dao.getActiveIterations("current-project").size(), is(1));
    }

    @Test
    public void getReadOnlyIterations() {
        assertThat(dao.getReadOnlyIterations("current-project").size(), is(1));
    }

    @Test
    public void getObsoleteIterations() {
        assertThat(dao.getObsoleteIterations("current-project").size(), is(1));
    }

    @Test
    public void getFilterProjectSizeAll() {
        assertThat(dao.getFilterProjectSize(false, false, false), is(4));
    }

    @Test
    public void getFilterProjectSizeOnlyActive() {
        assertThat(dao.getFilterProjectSize(false, true, true), is(2));
    }

    @Test
    public void getFilterProjectSizeOnlyReadOnly() {
        assertThat(dao.getFilterProjectSize(true, false, true), is(1));
    }

    @Test
    public void getFilterProjectSizeOnlyObsolete() {
        assertThat(dao.getFilterProjectSize(true, true, false), is(1));
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndReadOnly() {
        assertThat(dao.getFilterProjectSize(false, false, true), is(3));
    }

    @Test
    public void getFilterProjectSizeOnlyActiveAndObsolete() {
        assertThat(dao.getFilterProjectSize(false, true, false), is(3));
    }

    @Test
    public void getFilterProjectSizeOnlyObsoleteAndReadOnly() {
        assertThat(dao.getFilterProjectSize(true, false, false), is(2));
    }

    @Test
    public void getOffsetList() {
        List<HProject> projects =
                dao.getOffsetList(-1, -1, false, false, false);
        assertThat(projects.size(), is(4));
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

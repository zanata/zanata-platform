package org.zanata.dao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.rest.editor.dto.LocaleSortField;
import org.zanata.security.ZanataIdentity;

import com.google.common.collect.Lists;

public class LocaleDAOTest extends ZanataDbunitJpaTest {
    private LocaleDAO dao;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/TMXTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
    public void setup() {
        dao = new LocaleDAO((Session) getEm().getDelegate());
    }

    @Test
    public void createLocale() {
        HLocale locale = new HLocale(new LocaleId("en-ENABLED"));
        locale.setActive(true);
        locale.setEnabledByDefault(true);
        dao.makePersistent(locale);
        Long id = locale.getId();
        dao.flush();
        dao.clear();
        HLocale loadedLocale = dao.findById(id);
        assertThat(loadedLocale.getLocaleId().getId()).isEqualTo("en-ENABLED");
        assertThat(loadedLocale.isActive())
                .as("still active")
                .isTrue();
        assertThat(loadedLocale.isEnabledByDefault())
                .as("still enabled by default")
                .isTrue();
    }

    @Test
    public void findByLocaleReturnsCorrectLocale() {
        String id = "de";
        HLocale hl = dao.findByLocaleId(new LocaleId(id));
        assert hl != null;
        assertThat(hl.getLocaleId().getId()).isEqualTo(id);
    }

    @Test
    public void findByLocaleIdReturnsNullForNonexistentLocale() {
        String id = "nonexistentLocaleId";
        HLocale hl = dao.findByLocaleId(new LocaleId(id));
        assertThat(hl).isNull();
    }

    @Test
    public void testFind() {
        List<HLocale> results = dao.find(0, 1, "a", emptyList(), true);
        assertThat(results.size()).isEqualTo(1);

        results = dao.find(0, 10, "a", emptyList(), true);
        assertThat(results.size()).isEqualTo(4);
    }

    @Test
    public void testFindWithSort() {
        List<LocaleSortField> sortFields1 = Lists.newArrayList(
            LocaleSortField.getByField(LocaleSortField.LOCALE));
        List<HLocale> results1 = dao.find(0, 10, "e", sortFields1, true); // 5 results
        assertThat(results1.get(0).getLocaleId()).isEqualTo(LocaleId.DE);

        List<LocaleSortField> sortFields2 = Lists.newArrayList(
            LocaleSortField.getByField(LocaleSortField.MEMBER));
        List<HLocale> results2 = dao.find(0, 10, "e", sortFields2, true); // 5 results
        //first result of results2 can be ES or TE

        assertThat(results1.get(0)).isNotEqualTo(results2.get(0));
    }

    @Test
    public void getAllLocalesAndDocCount() {
        // expected localeIds
        LocaleId localeId1 = LocaleId.EN_US;
        LocaleId localeId2 = new LocaleId("as");

        Map<HLocale, Integer> results = dao.getAllSourceLocalesAndDocCount();
        assertThat(results).hasSize(2);

        List<LocaleId> returnedLocaleId =
                results.keySet().stream().map(hLocale -> hLocale.getLocaleId())
                        .collect(Collectors.toList());
        assertThat(returnedLocaleId).contains(localeId1, localeId2);
    }

    @Test
    public void getProjectSourceLocalesAndDocCount() {
        // expected localeIds
        LocaleId localeId1 = LocaleId.EN_US;
        LocaleId localeId2 = new LocaleId("as");

        Map<HLocale, Integer> results =
                dao.getProjectSourceLocalesAndDocCount("sample-project");
        assertThat(results).hasSize(2);

        List<LocaleId> returnedLocaleId =
                results.keySet().stream().map(hLocale -> hLocale.getLocaleId())
                        .collect(Collectors.toList());
        assertThat(returnedLocaleId).contains(localeId1, localeId2);
    }

    @Test
    public void getProjectVersionSourceLocalesAndDocCount() {
        // expected localeIds
        LocaleId localeId1 = LocaleId.EN_US;

        Map<HLocale, Integer> results =
                dao.getProjectVersionSourceLocalesAndDocCount("sample-project",
                        "1.0");
        assertThat(results).hasSize(1);

        List<LocaleId> returnedLocaleId =
                results.keySet().stream().map(hLocale -> hLocale.getLocaleId())
                        .collect(Collectors.toList());
        assertThat(returnedLocaleId).contains(localeId1);
    }

}

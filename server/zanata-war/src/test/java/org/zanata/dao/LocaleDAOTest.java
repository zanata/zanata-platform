package org.zanata.dao;

import com.google.common.collect.Lists;
import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.rest.editor.dto.LocaleSortField;
import org.zanata.security.ZanataIdentity;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class LocaleDAOTest extends ZanataDbunitJpaTest {
    private static final Logger log = LoggerFactory.getLogger(LocaleDAOTest.class);

    private LocaleDAO dao;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
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
    public void testFindByLocale() {
        log.debug("testFindByLocale");
        HLocale hl = dao.findByLocaleId(new LocaleId("de"));
        assertThat(hl.getLocaleId().getId(), is("de"));
    }

    @Test
    public void testFind() {
        List<HLocale> results = dao.find(0, 1, "a", null, true);
        assertThat(results.size(), is(1));

        results = dao.find(0, 10, "a", null, true);
        assertThat(results.size(), is(4));
    }

    @Test
    public void testFindWithSort() {
        List<LocaleSortField> sortFields1 = Lists.newArrayList(
            LocaleSortField.getByField(LocaleSortField.LOCALE));
        List<HLocale> results1 = dao.find(0, 10, "e", sortFields1, true); // 5 results
        assertThat(results1.get(0).getLocaleId(), is(LocaleId.DE));

        List<LocaleSortField> sortFields2 = Lists.newArrayList(
            LocaleSortField.getByField(LocaleSortField.MEMBER));
        List<HLocale> results2 = dao.find(0, 10, "e", sortFields2, true); // 5 results
        //first result of results2 can be ES or TE

        assertThat(results1.get(0), not(Matchers.equalTo(results2.get(0))));
    }

}

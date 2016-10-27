package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.security.ZanataIdentity;

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

}

package org.zanata.dao;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jboss.seam.security.Identity;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = { "jpa-tests" })
@Slf4j
public class GlossaryDAOTest extends ZanataDbunitJpaTest {
    private GlossaryDAO dao;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/GlossaryData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeClass
    void beforeClass() {
        Identity.setSecurityEnabled(false);
    }

    @BeforeMethod(firstTimeOnly = true)
    public void setup() {
        dao = new GlossaryDAO((Session) getEm().getDelegate());
    }

    @Test
    public void testGetEntryById() {
        log.debug("testGetEntryById");
        HGlossaryEntry entry = dao.getEntryById(1L);

        Assert.assertNotNull(entry);
        assertThat(entry.getGlossaryTerms().size(), is(3));
    }

    @Test
    public void testGetTermByLocaleId() {
        log.debug("testGetTermByLocaleId");
        List<HGlossaryEntry> entryList = dao.getEntriesByLocaleId(LocaleId.DE);
        assertThat(entryList.size(), is(1));
    }

    @Test
    public void testGetTermEntryAndLocale() {
        HGlossaryEntry mockEntry = mock(HGlossaryEntry.class);
        when(mockEntry.getId()).thenReturn(1L);

        log.debug("testGetTermEntryAndLocale");
        HGlossaryTerm term =
                dao.getTermByEntryAndLocale(mockEntry.getId(), LocaleId.DE);
        Assert.assertNotNull(term);

    }

    @Test
    public void testGetTermByGlossaryEntryId() {
        log.debug("testGetTermByGlossaryEntry");
        List<HGlossaryTerm> termList = dao.getTermByGlossaryEntryId(1L);
        assertThat(termList.size(), is(3));

    }

    @Test
    public void testGetEntryBySrcContentLocale() {
        log.debug("testGetEntryBySrcContentLocale");
        HGlossaryEntry entry =
                dao.getEntryBySrcLocaleAndContent(LocaleId.EN_US,
                        "test data content 1 (source lang)");
        Assert.assertNotNull(entry);
        assertThat(entry.getSrcLocale().getLocaleId(), is(LocaleId.EN_US));
    }
}

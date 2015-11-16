package org.zanata.dao;

import java.util.List;

import com.google.common.collect.Lists;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.GlossarySortField;
import org.zanata.common.LocaleId;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.security.ZanataIdentity;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
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
        List<HGlossaryEntry> entryList = dao.getEntriesByLocale(
            LocaleId.EN_US, 0, 1, "", null);
        assertThat(entryList.size(), is(1));
    }

    @Test
    public void testGetTermByLocaleId2() {
        log.debug("testGetTermByLocaleId");
        List<GlossarySortField> sortFields = Lists
            .newArrayList(
                GlossarySortField.getByField(GlossarySortField.SRC_CONTENT),
                GlossarySortField.getByField(GlossarySortField.PART_OF_SPEECH),
                GlossarySortField.getByField(GlossarySortField.DESCRIPTION));
        List<HGlossaryEntry> entryList = dao.getEntriesByLocale(
            LocaleId.EN_US, 0, 1, "", sortFields);
        assertThat(entryList.size(), is(1));
    }

    @Test
    public void testGetTermByLocaleId3() {
        log.debug("testGetTermByLocaleId");
        GlossarySortField POS =
                GlossarySortField.getByField("-"
                        + GlossarySortField.PART_OF_SPEECH);

        List<GlossarySortField> sortFields = Lists
            .newArrayList(POS);
        List<HGlossaryEntry> entryList = dao.getEntriesByLocale(
            LocaleId.EN_US, 0, 1, "", sortFields);
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
        HGlossaryEntry entry = dao.getEntryByContentHash("hash");
        Assert.assertNotNull(entry);
        assertThat(entry.getSrcLocale().getLocaleId(), is(LocaleId.EN_US));
    }

    @Test
    public void testGetEntriesByLocale() {
        List<GlossarySortField> sortFields = Lists.newArrayList();
        GlossarySortField content =
            GlossarySortField.getByField(GlossarySortField.SRC_CONTENT);
        GlossarySortField pos =
                GlossarySortField.getByField("-"
                        + GlossarySortField.PART_OF_SPEECH);

        sortFields.add(content);
        sortFields.add(pos);

        List<HGlossaryEntry> result =
                dao.getEntriesByLocale(LocaleId.EN_US, 0, 100, "", sortFields);
        assertThat(result.get(0).getPos(), is("pos 1"));
    }
}

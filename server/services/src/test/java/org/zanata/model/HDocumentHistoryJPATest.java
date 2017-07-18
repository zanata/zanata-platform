package org.zanata.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;

public class HDocumentHistoryJPATest extends ZanataDbunitJpaTest {
    private LocaleDAO localeDAO;
    HLocale de_DE;

    @Before
    public void beforeMethod() {
        localeDAO = new LocaleDAO((Session) em.getDelegate());
        de_DE = localeDAO.findByLocaleId(new LocaleId("de"));
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml"));
    }

    // FIXME this test only works if resources-dev is on the classpath
    @SuppressWarnings("deprecation")
    @Test
    public void ensureHistoryIsRecorded() {
        Session session = getSession();
        HDocument d =
                new HDocument("/path/to/document.txt", ContentType.TextPlain,
                        de_DE);
        d.setProjectIteration(session.load(HProjectIteration.class, 1L));
        session.save(d);
        session.flush();

        Date lastChanged = d.getLastChanged();

        d.incrementRevision();
        d.setContentType(ContentType.PO);
        session.update(d);
        session.flush();

        List<HDocumentHistory> historyElems = loadHistory(d);

        assertThat(historyElems.size(), is(1));
        HDocumentHistory history = historyElems.get(0);
        assertThat(history.getDocId(), is(d.getDocId()));
        assertThat(history.getContentType(), is(ContentType.TextPlain));
        assertThat(history.getLastChanged().getTime(),
                equalTo(lastChanged.getTime()));
        assertThat(history.getLastModifiedBy(), nullValue());
        assertThat(history.getLocale().getLocaleId(), is(de_DE.getLocaleId()));
        assertThat(history.getName(), is(d.getName()));
        assertThat(history.getPath(), is(d.getPath()));
        assertThat(history.getRevision(), is(d.getRevision() - 1));

        d.incrementRevision();
        d.setName("name2");
        session.update(d);
        session.flush();

        historyElems = loadHistory(d);
        assertThat(historyElems.size(), is(2));

    }

    @SuppressWarnings("unchecked")
    private List<HDocumentHistory> loadHistory(HDocument d) {
        return getSession().createCriteria(HDocumentHistory.class)
                .add(Restrictions.eq("document", d)).list();
    }

}

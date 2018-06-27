package org.zanata.model;

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

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(historyElems.size()).isEqualTo(1);
        HDocumentHistory history = historyElems.get(0);
        assertThat(history.getDocId()).isEqualTo(d.getDocId());
        assertThat(history.getContentType()).isEqualTo(ContentType.TextPlain);
        assertThat(history.getLastChanged().getTime())
                .isEqualTo(lastChanged.getTime());
        assertThat(history.getLastModifiedBy()).isNull();
        assertThat(history.getLocale().getLocaleId()).isEqualTo(de_DE.getLocaleId());
        assertThat(history.getName()).isEqualTo(d.getName());
        assertThat(history.getPath()).isEqualTo(d.getPath());
        assertThat(history.getRevision()).isEqualTo(d.getRevision() - 1);

        d.incrementRevision();
        d.setName("name2");
        session.update(d);
        session.flush();

        historyElems = loadHistory(d);
        assertThat(historyElems.size()).isEqualTo(2);

    }

    @SuppressWarnings("unchecked")
    private List<HDocumentHistory> loadHistory(HDocument d) {
        return getSession().createCriteria(HDocumentHistory.class)
                .add(Restrictions.eq("document", d)).list();
    }

}

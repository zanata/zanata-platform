package org.zanata.model;

import java.util.Arrays;
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
import org.zanata.dao.TextFlowTargetHistoryDAO;

import static org.assertj.core.api.Assertions.assertThat;

public class HTextFlowTargetHistoryJPATest extends ZanataDbunitJpaTest {
    private LocaleDAO localeDAO;
    private TextFlowTargetHistoryDAO historyDAO;
    HLocale en_US;
    HLocale de_DE;

    @Before
    public void beforeMethod() {
        localeDAO = new LocaleDAO((Session) em.getDelegate());
        historyDAO = new TextFlowTargetHistoryDAO((Session) em.getDelegate());
        en_US = localeDAO.findByLocaleId(LocaleId.EN_US);
        de_DE = localeDAO.findByLocaleId(new LocaleId("de"));
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    public void ensureHistoryIsRecorded() {
        Session session = getSession();
        HDocument d =
                new HDocument("/path/to/document.txt", ContentType.TextPlain,
                        en_US);
        d.setProjectIteration(session.load(HProjectIteration.class, 1L));
        session.save(d);
        session.flush();

        HTextFlow tf = new HTextFlow(d, "mytf", "hello world");
        d.getTextFlows().add(tf);
        session.flush();

        HTextFlowTarget target = new HTextFlowTarget(tf, de_DE);
        target.setContents("helleu world");
        session.save(target);
        session.flush();

        List<HTextFlowTargetHistory> historyElems = getHistory(target);
        assertThat(historyElems.size()).isEqualTo(0)
                .as("Incorrect History size on persist");

        target.setContents("blah!");
        session.flush();

        historyElems = getHistory(target);

        assertThat(historyElems.size()).isEqualTo(1)
                .as("Incorrect History size on first update");

        target.setContents("hola mundo!");
        session.flush();

        historyElems = getHistory(target);

        assertThat(historyElems.size()).isEqualTo(2)
                .as("Incorrect History size on second update");
        assertThat(historyElems.size()).isEqualTo(2);
        HTextFlowTargetHistory hist = historyElems.get(0);
        assertThat(hist.getContents()).isEqualTo(Arrays.asList("helleu world"));
    }

    @Test
    public void ensureHistoryIsRecordedPlural() {
        Session session = getSession();
        HDocument d =
                new HDocument("/path/to/document.txt", ContentType.TextPlain,
                        en_US);
        d.setProjectIteration(session.load(HProjectIteration.class, 1L));
        session.save(d);
        session.flush();

        HTextFlow tf = new HTextFlow(d, "mytf", "hello world");
        d.getTextFlows().add(tf);
        session.flush();

        HTextFlowTarget target = new HTextFlowTarget(tf, de_DE);
        target.setContents("helleu world", "helleu worlds");
        session.save(target);
        session.flush();

        List<HTextFlowTargetHistory> historyElems = getHistory(target);
        assertThat(historyElems.size()).isEqualTo(0);

        target.setContents("blah", "blah!");
        session.flush();

        historyElems = getHistory(target);

        assertThat(historyElems.size()).isEqualTo(1);
        HTextFlowTargetHistory hist = historyElems.get(0);
        assertThat(hist.getContents())
                .isEqualTo(Arrays.asList("helleu world", "helleu worlds"));

        assert historyDAO.findContentInHistory(target,
                Arrays.asList("helleu world", "helleu worlds"));
        assert !historyDAO.findContentInHistory(target,
                Arrays.asList("helleu world"));
        assert !historyDAO.findContentInHistory(target,
                Arrays.asList("helleu worlds"));
        assert !historyDAO.findContentInHistory(target,
                Arrays.asList("blah", "blah!"));
    }

    @SuppressWarnings("unchecked")
    private List<HTextFlowTargetHistory> getHistory(HTextFlowTarget tft) {
        return getSession().createCriteria(HTextFlowTargetHistory.class)
                .add(Restrictions.eq("textFlowTarget", tft)).list();
    }
}

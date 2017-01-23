package org.zanata.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.model.tm.TransMemory.tm;
import static org.zanata.model.tm.TransMemoryUnit.tu;
import static org.zanata.model.tm.TransMemoryUnitVariant.tuv;
import java.io.Serializable;
import java.util.ArrayList;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.util.CloseableIterator;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class TransMemoryStreamingDAOTest extends ZanataJpaTest {

    private TransMemoryStreamingDAO dao;
    private TransMemoryDAO transMemoryDAO;
    private Session session;

    @Before
    public void setup() {
        dao = new TransMemoryStreamingDAO(
                (HibernateEntityManagerFactory) getEmf());
        session = getSession();
        transMemoryDAO = new TransMemoryDAO(session);
    }

    @Test
    public void findAllTextFlows() throws Exception {
        deleteTMData();
        createTMData();
        session = newSession();
        Optional<TransMemory> transMemory = transMemoryDAO.getBySlug("testTM");
        CloseableIterator<TransMemoryUnit> iter =
                dao.findTransUnitsByTM(transMemory.get());
        try {
            assertThat(Iterators.size(iter), equalTo(4));
            deleteTMData();
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    private void createTMData() {
        TransMemory tm = tm("testTM");
        session.save(tm);
        String fr = LocaleId.FR.getId();
        String de = LocaleId.DE.getId();
        String sourceLoc = "en-US";
        ArrayList<TransMemoryUnit> tus = Lists.newArrayList(
                tu(tm, "doc0:resId0", "doc0:resId0", sourceLoc,
                        "<seg>source0</seg>", tuv(fr, "<seg>targetFR0</seg>"),
                        tuv(de, "<seg>targetDE0</seg>")),
                tu(tm, "doc0:resId1", "doc0:resId1", sourceLoc,
                        "<seg>SOURCE0</seg>", tuv(fr, "<seg>TARGETfr0</seg>")),
                tu(tm, "doc1:resId0", "doc1:resId0", sourceLoc,
                        "<seg>source0</seg>", tuv(fr, "<seg>targetFR0</seg>")),
                tu(tm, "doc1:resId1", "doc1:resId1", sourceLoc,
                        "<seg>SOURCE0</seg>", tuv(de, "<seg>TARGETde0</seg>")));
        for (TransMemoryUnit tu : tus) {
            session.save(tu);
        }
    }

    private void deleteTMData() {
        AbstractDAOImpl<TransMemoryUnitVariant, Long> tuvDao =
                getDao(TransMemoryUnitVariant.class);
        AbstractDAOImpl<TransMemoryUnit, Long> tuDao =
                getDao(TransMemoryUnit.class);
        tuvDao.deleteAll();
        tuDao.deleteAll();
        transMemoryDAO.deleteAll();
    }

    private <E, ID extends Serializable> AbstractDAOImpl<E, ID>
            getDao(Class<E> clazz) {
        return new AbstractDAOImpl<E, ID>(clazz, session);
    }
}

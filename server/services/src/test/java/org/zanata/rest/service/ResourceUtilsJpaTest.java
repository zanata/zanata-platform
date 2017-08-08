package org.zanata.rest.service;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.PerformanceProfiling;
import org.zanata.SlowTest;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ResourceType;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.zanata.test.CdiUnitRunner;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@RunWith(CdiUnitRunner.class)
public class ResourceUtilsJpaTest extends ZanataJpaTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ResourceUtilsJpaTest.class);

    @Inject
    private ResourceUtils resourceUtils;

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Before
    public void setUp() {
        deleteAllTables();
    }

    @Test
    @InRequestScope
    public void transferFromResourceMetadata() {
        ResourceMeta from = new ResourceMeta("resId");
        from.setContentType(ContentType.TextPlain);
        PoHeader poHeader = new PoHeader();
        poHeader.setComment("comment");
        poHeader.getEntries().add(new HeaderEntry("key", "value"));
        from.getExtensions(true).add(poHeader);
        from.setLang(LocaleId.ES);
        from.setName("name");
        from.setType(ResourceType.FILE);
        HLocale hLocale = new HLocale(LocaleId.EN_US);
        HDocument to = new HDocument("fullPath", ContentType.PO, hLocale);
        Set<String> commentExt = new HashSet<String>();
        commentExt.add(SimpleComment.ID);
        resourceUtils.transferFromResourceMetadata(from, to, commentExt,
                hLocale, 1);
        // TODO check the results in 'to'
    }
    // This should be executed manually in IDE
    // ideally change persistence.xml to use a local mysql database and monitor
    // general log etc.

    @Ignore("slow test")
    @Test
    @SlowTest
    @PerformanceProfiling
    @InRequestScope
    public void transferFromResource() {
        HLocale locale = EntityMakerBuilder.builder()
                .addConstructorParameterMaker(HLocale.class, 0,
                        FixedValueMaker.fix(LocaleId.ES))
                .build().makeAndPersist(getEm(), HLocale.class);
        HProjectIteration iteration = EntityMakerBuilder.builder()
                .addFieldOrPropertyMaker(HProject.class, "sourceViewURL",
                        FixedValueMaker.EMPTY_STRING_MAKER)
                .build().makeAndPersist(getEm(), HProjectIteration.class);
        Resource from = new Resource("message");
        from.setContentType(ContentType.PO);
        LocaleId localeId = locale.getLocaleId();
        // adjust this number to suit testing purpose
        int numOfTextFlows = 50;
        for (int i = 0; i < numOfTextFlows; i++) {
            addSampleTextFlow(from, localeId, i);
        }
        // this is the same from
        // org/zanata/service/impl/DocumentServiceImpl.java:140
        HDocument to =
                new HDocument(from.getName(), from.getContentType(), locale);
        to.setProjectIteration(iteration);
        getEm().persist(to);
        getEm().flush();
        log.info("======= start of transfer {} textflows ======",
                numOfTextFlows);
        Monitor monitor = MonitorFactory.start("transfer");
        resourceUtils.transferFromResource(from, to,
                Sets.newHashSet("gettext", "comment"), locale, 1);
        log.info("{}", monitor.stop());
        log.info("======= end of transfer =======");
        em.flush();
    }

    private static void addSampleTextFlow(Resource from, LocaleId localeId,
            int index) {
        from.getTextFlows().add(
                new TextFlow("res" + index, localeId, "hello world " + index));
    }
}

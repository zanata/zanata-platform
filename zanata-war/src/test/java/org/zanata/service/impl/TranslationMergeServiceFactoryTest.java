package org.zanata.service.impl;

import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.MergeType;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationMergeService;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class TranslationMergeServiceFactoryTest extends ZanataTest {

    @Inject
    private TranslationMergeServiceFactory factory;

    @Produces @Mock Session session;

    @Test
    @InRequestScope
    public void getMergeServiceWhenServerHasNoTarget() {
        TranslationMergeServiceFactory.MergeContext mergeContext =
                new TranslationMergeServiceFactory.MergeContext(null, null,
                        null, null, 1);
        TranslationMergeService result = factory.getMergeService(mergeContext);

        assertThat(result, Matchers.instanceOf(TranslationMergeFirstTran.class));
    }

    @Test
    @InRequestScope
    public void getMergeServiceWhenServerHasTargetAndMergeTypeIsAuto() {
        TranslationMergeServiceFactory.MergeContext mergeContext =
                new TranslationMergeServiceFactory.MergeContext(MergeType.AUTO,
                        null, null, new HTextFlowTarget(), 1);
        TranslationMergeService result = factory.getMergeService(mergeContext);

        assertThat(result, Matchers.instanceOf(TranslationMergeAuto.class));
    }

    @Test
    @InRequestScope
    public void getMergeServiceWhenServerHasTargetAndMergeTypeIsImport() {
        TranslationMergeServiceFactory.MergeContext mergeContext =
                new TranslationMergeServiceFactory.MergeContext(
                        MergeType.IMPORT, null, null, new HTextFlowTarget(), 1);
        TranslationMergeService result = factory.getMergeService(mergeContext);

        assertThat(result, Matchers.instanceOf(TranslationMergeImport.class));
    }

}

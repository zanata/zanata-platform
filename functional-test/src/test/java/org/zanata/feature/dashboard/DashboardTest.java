/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.feature.dashboard;

import com.github.huangp.entityunit.entity.Callbacks;
import com.github.huangp.entityunit.entity.EntityMaker;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.entity.TakeCopyCallback;
import com.github.huangp.entityunit.entity.WireManyToManyCallback;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.github.huangp.entityunit.maker.IntervalValuesMaker;
import com.github.huangp.entityunit.maker.RangeValuesMaker;
import com.github.huangp.entityunit.maker.SkipFieldValueMaker;
import lombok.extern.slf4j.Slf4j;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.zanata.common.ActivityType;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.page.utility.DashboardPage;
import org.zanata.util.EntityManagerFactoryHolder;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(ConcordionRunner.class)
@Extensions({ ScreenshotExtension.class, TimestampFormatterExtension.class,
        CustomResourceExtension.class })
@Category(ConcordionTest.class)
@Slf4j
public class DashboardTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Before
    public void setUp() {
        EntityManager entityManager =
                EntityManagerFactoryHolder.holder().getEmFactory()
                        .createEntityManager();

        // here we use glossarist to test because the query to fetch activity is
        // cachable. Unless we use cache provider that supports clustering we
        // will have to avoid hitting any query cache.
        WireManyToManyCallback manyToManyCallback = Callbacks
            .wireManyToMany(HProject.class, sampleProjectRule.getGlossarist());
        TakeCopyCallback copyCallback = Callbacks.takeCopy();
        EntityMakerBuilder.builder()
            .reuseEntities(sampleProjectRule.getGlossarist(),
                sampleProjectRule.getGlossarist().getAccount())
            .build()
            .makeAndPersist(entityManager, HTextFlowTarget.class,
                Callbacks.chain(manyToManyCallback, copyCallback));

        EntityMakerBuilder builder = EntityMakerBuilder.builder()
            .reuseEntities(copyCallback.getCopy())
                // public Activity(HPerson actor, IsEntityWithType
                // context, IsEntityWithType target, ActivityType
                // activityType, int wordCount)
            .addConstructorParameterMaker(
                Activity.class, 0,
                FixedValueMaker.fix(sampleProjectRule
                    .getGlossarist()))
            .addConstructorParameterMaker(Activity.class, 1,
                FixedValueMaker.fix(
                    copyCallback.getByType(HProjectIteration.class)))
            .addConstructorParameterMaker(
                Activity.class, 2,
                RangeValuesMaker.cycle(
                    copyCallback.getByType(HDocument.class),
                    copyCallback.getByType(HTextFlowTarget.class)))
            .addConstructorParameterMaker(
                Activity.class, 3,
                RangeValuesMaker
                    .cycle(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        ActivityType.UPDATE_TRANSLATION,
                        ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                        ActivityType.REVIEWED_TRANSLATION))
            .addConstructorParameterMaker(Activity.class, 4,
                FixedValueMaker.fix(10))
                // below two fields are primitive types. It can not tell
                // whether it has default value or not so we have to
                // skip them
            .addFieldOrPropertyMaker(Activity.class, "contextId",
                SkipFieldValueMaker.MAKER)
            .addFieldOrPropertyMaker(Activity.class, "lastTargetId",
                SkipFieldValueMaker.MAKER)
            .addFieldOrPropertyMaker(
                Activity.class, "creationDate",
                        IntervalValuesMaker.startFrom(new Date(),
                            -TimeUnit.DAYS.toMillis(1)));

        EntityMaker maker = builder.build();

        // make 6 activities
        Activity activity = maker.makeAndPersist(entityManager, Activity.class);
        log.info("activity: {} - {} - {} - {}", activity.getContextType(),
                activity.getContextId(), activity.getActivityType(),
            activity.getActor());
        maker.makeAndPersist(entityManager, Activity.class);
        maker.makeAndPersist(entityManager, Activity.class);
        maker.makeAndPersist(entityManager, Activity.class);
        maker.makeAndPersist(entityManager, Activity.class);
        maker.makeAndPersist(entityManager, Activity.class);
    }

    private DashboardPage dashboardPage;

    public boolean signInAs(String username, String password) {
        dashboardPage = new LoginWorkFlow().signIn(username, password);

        return dashboardPage.hasLoggedIn();
    }

    public boolean hasMyActivitiesSection() {
        return dashboardPage.containActivityListSection();
    }

    public boolean hasMaintainedProjectsSection() {
        return dashboardPage.containMyMaintainedProjectsSection();
    }

    public void gotoDashboard() {
        dashboardPage = new BasicWorkFlow().goToDashboard();
    }

    public boolean myActivitiesListNotEmpty() {
        return !dashboardPage.getMyActivityList().isEmpty();
    }

    public int myActivitiesCount() {
        return dashboardPage.getMyActivityList().size();
    }

    public boolean myActivitiesCountIsMoreThan(int compareTo) {
        return dashboardPage.getMyActivityList().size() > compareTo;
    }

    public boolean maintainedProjectNotEmpty() {
        return !dashboardPage.getMyMaintainedProject().isEmpty();
    }

    public void clickMoreActivity() {
        dashboardPage.clickMoreActivity();
    }
}

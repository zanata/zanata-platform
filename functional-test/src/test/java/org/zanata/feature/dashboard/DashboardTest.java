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
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.entity.TakeCopyCallback;
import com.github.huangp.entityunit.entity.WireManyToManyCallback;
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
import org.zanata.model.IsEntityWithType;
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
        EntityMakerBuilder
                .builder()
                .reuseEntities(sampleProjectRule.getGlossarist(),
                        sampleProjectRule.getGlossarist().getAccount())
                .build()
                .makeAndPersist(entityManager, HDocument.class,
                        Callbacks.chain(manyToManyCallback, copyCallback));

        HDocument hDocument = copyCallback.getByType(HDocument.class);

        makeActivity(entityManager, copyCallback, hDocument,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 1);
        makeActivity(entityManager, copyCallback, hDocument,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 2);
        makeActivity(entityManager, copyCallback, hDocument,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 3);
        makeActivity(entityManager, copyCallback, hDocument,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 4);
        makeActivity(entityManager, copyCallback, hDocument,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 5);
        makeActivity(entityManager, copyCallback, hDocument,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 6);
    }

    private void makeActivity(EntityManager entityManager,
        TakeCopyCallback copyCallback,
        IsEntityWithType activityTarget, ActivityType activityType,
        int pastDays) {
        Activity activity = new Activity(sampleProjectRule.getGlossarist(),
            copyCallback.getByType(HProjectIteration.class),
            activityTarget,
            activityType, 10);
        Date now = new Date();
        long timestamp = now.getTime() - TimeUnit.DAYS.toMillis(pastDays);
        activity.setCreationDate(new Date(timestamp));
        entityManager.getTransaction().begin();
        entityManager.persist(activity);
        entityManager.getTransaction().commit();
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

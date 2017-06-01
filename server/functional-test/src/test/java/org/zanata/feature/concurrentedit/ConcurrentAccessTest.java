/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.concurrentedit;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.util.ZanataRestCaller;
import com.google.common.base.Throwables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ConcurrentAccessTest extends ZanataTestCase {

    @BeforeClass
    // Need to ensure that the correct concurrent slots are available
    public static void beforeClass() throws Exception {
        String path = "rest/configurations/c/max.concurrent.req.per.apikey";
        Invocation.Builder configRequest = clientRequestAsAdmin(path);
        configRequest.put(Entity.json("6"));
    }

    @Feature(summary = "The system will handle concurrent document " +
            "creation gracefully",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void concurrentDocumentCreationWillNotCauseHibernateException()
            throws InterruptedException {
        final String projectSlug = "project";
        final String iterationSlug = "master";
        final AtomicInteger counter = new AtomicInteger(1);
        new ZanataRestCaller().createProjectAndVersion(projectSlug,
                iterationSlug, "gettext");

        int threadCount = 5;
        Callable<Integer> task = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int suffix = counter.getAndIncrement();
                return new ZanataRestCaller().postSourceDocResource(
                        projectSlug, iterationSlug, buildResource(suffix),
                        false);
            }
        };
        List<Callable<Integer>> tasks = Collections.nCopies(threadCount, task);
        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        List<Future<Integer>> futures = executorService.invokeAll(tasks);

        List<Integer> result = getStatusCodes(futures);

        List<Integer> expectedReturnCode =
                Collections.nCopies(threadCount, 201);
        assertThat(result).isEqualTo(expectedReturnCode);
    }

    private static Resource buildResource(int suffix) {
        return buildSourceResource("doc" + suffix,
                buildTextFlow("res" + suffix, "content" + suffix));
    }

    private static List<Integer> getStatusCodes(List<Future<Integer>> futures) {
        return futures.stream().map(ConcurrentAccessTest::getResult)
                .collect(Collectors.toList());
    }

    private static Integer getResult(Future<Integer> input) {
        try {
            return input.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Invocation.Builder clientRequestAsAdmin(String path) {
        return new ResteasyClientBuilder()
                .build()
                .target(PropertiesHolder.getProperty(Constants.zanataInstance
                        .value()) + path)
                .request(MediaType.APPLICATION_XML_TYPE)
                .header("X-Auth-User", "admin")
                .header("X-Auth-Token",
                        PropertiesHolder.getProperty(Constants.zanataApiKey
                                .value()));
    }
}

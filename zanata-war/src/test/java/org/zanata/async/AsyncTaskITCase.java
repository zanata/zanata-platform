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
package org.zanata.async;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.zanata.async.AsyncTaskResult.taskResult;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Test;
import org.zanata.ArquillianTest;

import com.google.common.collect.Lists;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncTaskITCase extends ArquillianTest {

    @Inject
    TestAsyncBean testAsyncBean;

    @Override
    protected void prepareDBUnitOperations() {
    }

    @Test
    public void taskReturnsValue() throws Exception {
        // Given an expected return value
        final String expectedRetVal = "EXPECTED";

        // Start an asynchronous process
        Future<String> result = testAsyncBean.asyncString();

        // Wait for it to finish and get the result
        String resultVal = result.get();

        // Must be the same as the component that was inserted outside of the
        // task
        assertThat(resultVal).isEqualTo(expectedRetVal);
    }

    @Test
    public void taskDoesNotReturnValue() throws Exception {
        // Given a task handle
        AsyncTaskHandle handle = new AsyncTaskHandle();

        // Start an asynchronous process
        testAsyncBean.doesNotReturn(handle);

        // Wait for it to finish and get the result
        handle.getResult();

        // Must have executed the logic inside the method
        assertThat(handle.getCurrentProgress()).isEqualTo(100);
    }

    @Test
    public void executionError() throws Exception {
        // Start an asynchronous process that throws an exception
        try {
            Future<String> result = testAsyncBean.throwsError();
            result.get();
            failBecauseExceptionWasNotThrown(ExecutionException.class);
        } catch (Exception e) {
            // Original exception is wrapped around a
            // java.concurrent.ExecutionException
            assertThat(e.getCause()).hasMessage("Expected Exception");
        }
    }

    @Test
    public void progressUpdates() throws Exception {
        final List<Integer> progressUpdates = Lists.newArrayList();

        // Custom handle so that progress updates are recorded
        final AsyncTaskHandle<Void> taskHandle =
                new AsyncTaskHandle<Void>() {
                    @Override
                    public void setCurrentProgress(int progress) {
                        super.setCurrentProgress(progress);
                        progressUpdates.add(progress);
                    }
                };

        // Start an asynchronous process that updates its progress
        Future result = testAsyncBean.progressUpdates(taskHandle);

        // Wait for it to finish
        result.get();

        // Progress update calls should match the task's internal updates
        assertThat(taskHandle.getCurrentProgress()).isEqualTo(100);
        assertThat(progressUpdates.size()).isEqualTo(4);
        assertThat(progressUpdates).contains(25, 50, 75, 100);
    }


    @Named("testAsyncBean")

    @ContainsAsyncMethods
    public static class TestAsyncBean {

        @Async
        public Future<String> asyncString() {
            return taskResult("EXPECTED");
        }

        @Async
        public void doesNotReturn(AsyncTaskHandle handle) {
            handle.setCurrentProgress(100);
            return;
        }

        @Async
        public Future<String> throwsError() {
            throw new RuntimeException("Expected Exception");
        }

        @Async
        public Future progressUpdates(AsyncTaskHandle handle) {
            handle.setCurrentProgress(25);
            handle.setCurrentProgress(50);
            handle.setCurrentProgress(75);
            handle.setCurrentProgress(100);
            return taskResult();
        }
    }
}

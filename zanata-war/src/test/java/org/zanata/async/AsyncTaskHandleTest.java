/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncTaskHandleTest {

    @Test
    public void testStartTiming() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        handle.startTiming();

        assertThat(handle.isStarted()).isTrue();
        assertThat(handle.getStartTime()).isGreaterThan(0);
    }

    @Test
    public void testFinishTiming() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        handle.startTiming();
        Thread.sleep(10); // Sleep as if something was executed
        handle.finishTiming();

        assertThat(handle.getFinishTime()).isGreaterThan(0);
        assertThat(handle.getFinishTime()).isGreaterThan(handle.getStartTime());
    }

    @Test
    public void testResult() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        AsyncTaskResult result = new AsyncTaskResult();
        handle.setFutureResult(result);

        result.set("result");

        assertThat(handle.getResult()).isEqualTo("result");
    }

    @Test(expectedExceptions = Exception.class,
            expectedExceptionsMessageRegExp = ".*Exception thrown.*")
    public void testException() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        AsyncTaskResult result = new AsyncTaskResult();
        handle.setFutureResult(result);

        result.setException(new Exception("Exception thrown"));

        handle.getResult();
    }

    @Test
    public void testIsDone() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        AsyncTaskResult result = new AsyncTaskResult();
        handle.setFutureResult(result);

        result.set("result");

        assertThat(handle.isDone()).isTrue();
    }

    @Test
    public void testIsCancelled() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        AsyncTaskResult result = new AsyncTaskResult();
        handle.setFutureResult(result);
        handle.cancel(true);

        assertThat(handle.isCancelled()).isTrue();
    }

    @Test
    public void testEstimatedTimeRemaining() throws Exception {
        AsyncTaskHandle handle = new AsyncTaskHandle();
        handle.setMinProgress(0);
        handle.setMaxProgress(10);
        AsyncTaskResult result = new AsyncTaskResult();
        handle.setFutureResult(result);
        handle.startTiming();

        assertThat(handle.getEstimatedTimeRemaining().isPresent()).describedAs(
                "Estimated time remaining is not available").isFalse();

        handle.increaseProgress(1);
        assertThat(handle.getEstimatedTimeRemaining().isPresent()).isTrue();
    }
}

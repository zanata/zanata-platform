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
package org.zanata.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.common.base.Optional;

/**
 * Asynchronous handle to provide communication between an asynchronous task and
 * interested clients.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncTaskHandle<V> {

    private Future<V> futureResult;
    public int maxProgress = 100;
    public int minProgress = 0;
    public int currentProgress = 0;
    private long startTime = -1;
    private long finishTime = -1;

    public int increaseProgress(int increaseBy) {
        currentProgress += increaseBy;
        return currentProgress;
    }

    void startTiming() {
        startTime = System.currentTimeMillis();
    }

    void finishTiming() {
        finishTime = System.currentTimeMillis();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureResult.cancel(mayInterruptIfRunning);
    }

    public V getResult() throws InterruptedException, ExecutionException {
        return futureResult.get();
    }

    public V getResult(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return futureResult.get(timeout, unit);
    }

    public boolean isDone() {
        return futureResult != null && futureResult.isDone();
    }

    public boolean isCancelled() {
        return futureResult != null && futureResult.isCancelled();
    }

    public boolean isStarted() {
        return startTime >= 0;
    }

    /**
     * @return An optional container with the estimated time remaining for the
     *         process to finish, or an empty container if the time cannot be
     *         estimated.
     */
    public Optional<Long> getEstimatedTimeRemaining() {
        if (this.startTime > 0 && currentProgress > 0) {
            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - this.startTime;
            int remainingUnits = this.maxProgress - this.currentProgress;
            return Optional
                    .of(timeElapsed * remainingUnits / this.currentProgress);
        } else {
            return Optional.absent();
        }
    }

    /**
     * @return The time that the task has been executing for, or the total
     *         execution time if the task has finished (in milliseconds).
     */
    public long getExecutingTime() {
        if (startTime > 0) {
            if (finishTime > startTime) {
                return finishTime - startTime;
            } else {
                return System.currentTimeMillis() - startTime;
            }
        } else {
            return 0;
        }
    }

    /**
     * @return The estimated elapsed time (in milliseconds) from the start of
     *         the process.
     */
    public long getTimeSinceStart() {
        if (this.startTime > 0) {
            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - this.startTime;
            return timeElapsed;
        } else {
            return 0;
        }
    }

    /**
     * @return The estimated elapsed time (in milliseconds) from the finish of
     *         the process.
     */
    public long getTimeSinceFinish() {
        if (finishTime > 0) {
            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - finishTime;
            return timeElapsed;
        } else {
            return 0;
        }
    }

    void setFutureResult(final Future<V> futureResult) {
        this.futureResult = futureResult;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public void setMaxProgress(final int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getMinProgress() {
        return this.minProgress;
    }

    public void setMinProgress(final int minProgress) {
        this.minProgress = minProgress;
    }

    public int getCurrentProgress() {
        return this.currentProgress;
    }

    public void setCurrentProgress(final int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getFinishTime() {
        return this.finishTime;
    }
}

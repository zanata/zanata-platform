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

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.zanata.security.ZanataIdentity;

import com.google.common.base.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Asynchronous handle to provide communication between an asynchronous task and
 * interested clients.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
// TODO this doesn't look thread safe
public class AsyncTaskHandle<V> implements Serializable {

    private static final long serialVersionUID = -2896367626313653728L;
    @SuppressFBWarnings("SE_BAD_FIELD")
    private CompletableFuture<V> futureResult;
    private long maxProgress = 100;
    private long currentProgress = 0;
    private long startTime = -1;
    private long finishTime = -1;
    private String cancelledBy;
    private long cancelledTime;
    private String keyId;
    protected String taskName;

    public static boolean taskIsNotRunning(
            @Nullable AsyncTaskHandle<?> handleByKey) {
        return handleByKey == null || handleByKey.isCancelled()
                || handleByKey.isDone();
    }

    public boolean isRunning() {
        return isStarted() && !isCancelled() && !isDone();
    }

    public long increaseProgress(long increaseBy) {
        currentProgress += increaseBy;
        return currentProgress;
    }

    protected void startTiming() {
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
            long remainingUnits = this.maxProgress - this.currentProgress;
            return Optional
                    .of(timeElapsed * remainingUnits / this.currentProgress);
        } else {
            return Optional.absent();
        }
    }

    public boolean isVisibleTo(ZanataIdentity identity) {
        return isAdminOrSameUser(this, identity);
    }

    public boolean canCancel(ZanataIdentity identity) {
        return isAdminOrSameUser(this, identity);
    }

    private static boolean isAdminOrSameUser(AsyncTaskHandle<?> taskHandle,
            ZanataIdentity identity) {
        return identity != null && (identity.hasRole("admin")
                || triggeredBySameUser(taskHandle, identity));
    }

    public static boolean triggeredBySameUser(AsyncTaskHandle<?> taskHandle,
            ZanataIdentity identity) {
        return taskHandle instanceof UserTriggeredTaskHandle && Objects.equals(
                ((UserTriggeredTaskHandle) taskHandle).getTriggeredBy(),
                identity.getAccountUsername());
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

    protected void setFutureResult(final CompletableFuture<V> futureResult) {
        this.futureResult = futureResult;
    }

    public long getMaxProgress() {
        return this.maxProgress;
    }

    public void setMaxProgress(final long maxProgress) {
        this.maxProgress = maxProgress;
    }

    public long getCurrentProgress() {
        return this.currentProgress;
    }

    protected void setCurrentProgress(final long currentProgress) {
        this.currentProgress = currentProgress;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getFinishTime() {
        return this.finishTime;
    }

    /**
     * Registers a callback to be run when the task completes. Keep in mind this
     * callback is run in the same context as the task itself.
     *
     * @param action the action to take when the task is completed. The action
     *               itself accepts a result and a Throwable. The throwable will
     *               be null if the task has completed successfully.
     * @see CompletableFuture#whenComplete(BiConsumer)
     */
    public void whenTaskComplete(BiConsumer<V, Throwable> action) {
        futureResult = futureResult.whenComplete(action);
    }

    public String getCancelledBy() {
        return this.cancelledBy;
    }

    public void setCancelledBy(final String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public long getCancelledTime() {
        return this.cancelledTime;
    }

    public void setCancelledTime(final long cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setTaskName(String name) {
        this.taskName = name;
    }

    /**
     * Provide a user visible name for the task
     * @return task name, or generic class name response if not set.
     */
    public String getTaskName() {
        return ObjectUtils.firstNonNull(this.taskName, this.getClass().getName());
    }
}

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

import javax.annotation.Nullable;

import com.google.common.util.concurrent.AbstractFuture;

import lombok.Getter;
import lombok.Setter;

/**
 * Asynchronous handle to provide communication between an asynchronous task and
 * interested clients.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncTaskHandle<V> extends AbstractFuture<V> {

    @Getter
    private final String taskName;

    @Getter
    @Setter
    public int maxProgress = 100;

    @Getter
    @Setter
    public int minProgress = 0;

    @Getter
    @Setter
    public int currentProgress = 0;

    public AsyncTaskHandle(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"(taskName="+taskName+")";
    }

    @Override
    protected boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }

    @Override
    protected boolean set(@Nullable V value) {
        return super.set(value);
    }

    public int increaseProgress(int increaseBy) {
        currentProgress += increaseBy;
        return currentProgress;
    }

    /**
     * Cancels the task without trying to forcefully interrupt the task. This is
     * equivalent to calling <code>cancel(false)</code>
     *
     * @see AsyncTaskHandle#cancel(boolean)
     * @return false if the task could not be cancelled, typically because it
     *         has already completed normally; true otherwise
     */
    public boolean cancel() {
        return cancel(false);
    }

    /**
     * Cancels the task, forcefully cancelling the task if needed. This is
     * equivalent to calling <code>cancel(true)</code>
     *
     * @see AsyncTaskHandle#cancel(boolean)
     * @return false if the task could not be cancelled, typically because it
     *         has already completed normally; true otherwise
     */
    public boolean forceCancel() {
        return cancel(true);
    }
}

/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.async.handle;

import org.zanata.async.AsyncTaskHandle;

/**
 * Asynchronous task handle for the merge translations process.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MergeTranslationsTaskHandle extends AsyncTaskHandle<Void> {

    private int totalTranslations;
    private String cancelledBy;
    private long cancelledTime;
    private String triggeredBy;

    public int getTotalTranslations() {
        return this.totalTranslations;
    }

    public void setTotalTranslations(final int totalTranslations) {
        this.totalTranslations = totalTranslations;
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

    public String getTriggeredBy() {
        return this.triggeredBy;
    }

    public void setTriggeredBy(final String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}

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
package org.zanata.model;

import org.zanata.common.ContentState;

public class StatusCount {
    public final ContentState status;
    public final Long count;

    @java.beans.ConstructorProperties({ "status", "count" })
    public StatusCount(final ContentState status, final Long count) {
        this.status = status;
        this.count = count;
    }

    public ContentState getStatus() {
        return this.status;
    }

    public Long getCount() {
        return this.count;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof StatusCount))
            return false;
        final StatusCount other = (StatusCount) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null
                : !this$status.equals(other$status))
            return false;
        final Object this$count = this.getCount();
        final Object other$count = other.getCount();
        if (this$count == null ? other$count != null
                : !this$count.equals(other$count))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StatusCount;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $count = this.getCount();
        result = result * PRIME + ($count == null ? 43 : $count.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "StatusCount(status=" + this.getStatus() + ", count="
                + this.getCount() + ")";
    }
}

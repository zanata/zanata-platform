/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.events;

import org.zanata.model.HProject;
// TODO use HProjectIteration with a discriminator instead?

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public final class ProjectUpdate {
    private final HProject project;
    private final String oldSlug;

    @java.beans.ConstructorProperties({ "project", "oldSlug" })
    public ProjectUpdate(final HProject project, final String oldSlug) {
        this.project = project;
        this.oldSlug = oldSlug;
    }

    public HProject getProject() {
        return this.project;
    }

    public String getOldSlug() {
        return this.oldSlug;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ProjectUpdate))
            return false;
        final ProjectUpdate other = (ProjectUpdate) o;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null
                : !this$project.equals(other$project))
            return false;
        final Object this$oldSlug = this.getOldSlug();
        final Object other$oldSlug = other.getOldSlug();
        if (this$oldSlug == null ? other$oldSlug != null
                : !this$oldSlug.equals(other$oldSlug))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $oldSlug = this.getOldSlug();
        result = result * PRIME + ($oldSlug == null ? 43 : $oldSlug.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ProjectUpdate(project=" + this.getProject() + ", oldSlug="
                + this.getOldSlug() + ")";
    }
}

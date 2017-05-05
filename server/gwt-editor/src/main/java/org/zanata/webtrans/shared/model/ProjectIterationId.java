package org.zanata.webtrans.shared.model;

import org.zanata.common.ProjectType;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class ProjectIterationId implements IsSerializable, Serializable {
    private String projectSlug;
    private String iterationSlug;
    private ProjectType projectType;

    // for GWT
    @SuppressWarnings("unused")
    public ProjectIterationId() {
    }

    public ProjectIterationId(String projectSlug, String iterationSlug,
            ProjectType projectType) {
        this.projectSlug = projectSlug;
        this.iterationSlug = iterationSlug;
        this.projectType = projectType;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public String getIterationSlug() {
        return iterationSlug;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    @Override
    public String toString() {
        return projectSlug + "/" + iterationSlug;
    }

    public static ProjectIterationId valueOf(String value) {
        String[] parts = value.split("/");
        Preconditions.checkArgument(parts.length == 2, "must be in format: projectSlug/versionSlug");
        return new ProjectIterationId(parts[0], parts[1], null);
    }

    @Override
    public int hashCode() {
        return (projectSlug + "/" + iterationSlug).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof ProjectIterationId) {
            ProjectIterationId other = (ProjectIterationId) obj;
            return other.projectSlug.equals(projectSlug)
                    && other.iterationSlug.equals(iterationSlug);
        }
        return false;
    }

    public static ProjectIterationId of(String projectSlug,
            String iterationSlug, ProjectType projectType) {
        return new ProjectIterationId(projectSlug, iterationSlug, projectType);
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;

    }
}

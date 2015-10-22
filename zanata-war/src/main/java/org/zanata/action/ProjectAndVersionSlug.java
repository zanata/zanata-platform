package org.zanata.action;

import java.io.Serializable;

import org.apache.deltaspike.core.api.scope.ViewAccessScoped;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ViewAccessScoped
public class ProjectAndVersionSlug implements Serializable {
    private static final long serialVersionUID = 5968985635386162165L;
    private String projectSlug;
    private String versionSlug;

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public String getVersionSlug() {
        return versionSlug;
    }

    public void setVersionSlug(String versionSlug) {
        this.versionSlug = versionSlug;
    }
}

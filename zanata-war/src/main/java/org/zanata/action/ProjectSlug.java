package org.zanata.action;

import java.io.Serializable;

import org.apache.deltaspike.core.api.scope.ViewAccessScoped;

/**
 * We set this to ViewAccessScoped so that it can retain the value accross
 * requests (original request, subsequent ajax requests)
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ViewAccessScoped
public class ProjectSlug implements Serializable {
    private static final long serialVersionUID = -2815530471069045162L;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

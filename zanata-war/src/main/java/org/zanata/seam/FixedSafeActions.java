package org.zanata.seam;

import static org.jboss.seam.annotations.Install.DEPLOYMENT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.inject.Named;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.zanata.util.ServiceLocator;

/**
 * Maintains a set of "safe" actions that may be performed by &lt;s:link/&gt;,
 * as determined by actually parsing the view.
 *
 * @author Gavin King
 *
 */
@javax.enterprise.context.ApplicationScoped
@BypassInterceptors
@Named("org.jboss.seam.navigation.safeActions")
@Install(precedence = DEPLOYMENT,
        classDependencies = "javax.faces.context.FacesContext")
// Implementation copied from
// https://source.jboss.org/browse/Seam/branches/community/Seam_2_3/jboss-seam/src/main/java/org/jboss/seam/navigation/SafeActions.java?r=14141
// following https://community.jboss.org/message/688860#688860
// TODO see if this can be removed - see also https://issues.jboss.org/browse/JBSEAM-4800
public class FixedSafeActions extends org.jboss.seam.navigation.SafeActions {

    private Set<String> safeActions = Collections
            .synchronizedSet(new HashSet<String>());

    public static String toActionId(String viewId, String expression) {
        return viewId.substring(1) + ':'
                + expression.substring(2, expression.length() - 1);
    }

    public static String toAction(String id) {
        int loc = id.indexOf(':');
        if (loc < 0)
            throw new IllegalArgumentException();
        return "#{" + id.substring(loc + 1) + "}";
    }

    public void addSafeAction(String id) {
        safeActions.add(id);
    }

    public boolean isActionSafe(String id) {
        if (safeActions.contains(id))
            return true;

        int loc = id.indexOf(':');
        if (loc < 0)
            throw new IllegalArgumentException("Invalid action method " + id);
        String viewId = id.substring(0, loc);
        String action = "\"#{" + id.substring(loc + 1) + "}\"";

        // adding slash as it otherwise won't find a page viewId by getResource*
        InputStream is =
                FacesContext.getCurrentInstance().getExternalContext()
                        .getResourceAsStream("/" + viewId);
        if (is == null)
            throw new IllegalStateException("Unable to read view " + "/"
                    + viewId + " to execute action " + action);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while (reader.ready()) {
                if (reader.readLine().contains(action)) {
                    addSafeAction(id);
                    return true;
                }
            }
            return false;
        } catch (IOException ioe) {
            throw new RuntimeException("Error parsing view " + "/" + viewId
                    + " to execute action " + action, ioe);
        } finally {
            try {
                reader.close();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public static FixedSafeActions instance() {
        if (!Contexts.isApplicationContextActive()) {
            throw new IllegalStateException("No active application context");
        }
        return ServiceLocator.instance().getInstance(FixedSafeActions.class);
    }

}

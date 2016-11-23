package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.push.PushOptions;

/**
 * Pushes source text to a Zanata project version so that it can be translated,
 * and optionally push translated text as well. NB: Any documents which exist on
 * the server but not locally will be deleted as obsolete.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Mojo(name = "push", requiresOnline = true, requiresProject = false)
public class PushSimpleMojo extends AbstractPushMojo implements PushOptions {
    /**
     * Obsolete option, only for backwards compatibility
     */
    @Parameter(property = "zanata.useSrcOrder", defaultValue = "false")
    @Deprecated
    private boolean useSrcOrder;

    /**
     * Whether module processing should be enabled. This option is obsolete.
     * Please use push-module instead.
     */
    @Parameter(property = "zanata.enableModules")
    private boolean enableModules = false;

    public PushSimpleMojo() {
        if (enableModules) {
            throw new RuntimeException(
                    "Please use push-module for module support");
        }
    }

    @Override
    public boolean getEnableModules() {
        return false;
    }

    @Override
    public boolean getDeleteObsoleteModules() {
        // False for Simple push
        return false;
    }
}

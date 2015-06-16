package org.zanata.maven;

import org.zanata.client.commands.push.PushOptions;

/**
 * Pushes source text to a Zanata project version so that it can be translated,
 * and optionally push translated text as well. NB: Any documents which exist on
 * the server but not locally will be deleted as obsolete.
 *
 * @goal push
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PushSimpleMojo extends AbstractPushMojo implements PushOptions {
    /**
     * Obsolete option, only for backwards compatibility
     *
     * @parameter expression="${zanata.useSrcOrder}" default-value="false"
     */
    @Deprecated
    private boolean useSrcOrder;

    /**
     * Whether module processing should be enabled. This option is obsolete.
     * Please use push-module instead.
     *
     * @parameter expression="${zanata.enableModules}"
     */
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

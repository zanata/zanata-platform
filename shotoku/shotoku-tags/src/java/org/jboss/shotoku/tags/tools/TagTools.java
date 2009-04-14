package org.jboss.shotoku.tags.tools;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.tags.service.TagServiceImpl;
import org.jboss.shotoku.tags.TagService;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TagTools {
    private static TagService instance;

    /**
     * Gets an instance of Shotoku tag service - this should be always the same
     * the object, so the dirty sets work properly.
     * @return An instance of org.jboss.shotoku.svn.SvnService
     */
    public synchronized static TagService getService() {
        try {
            if (instance == null) {
                if (ContentManager.isEmbedded()) {
                    // Embedded mode - simply creating a new service instance.
                    instance = new TagServiceImpl();
                    instance.create();
                    instance.start();
                } else {
                    // Application server mode - creating a proxy to a mbean.
                    instance = (TagService) MBeanProxyExt.create(
                            TagService.class,
                            Constants.TAG_SERVICE_NAME,
                            MBeanServerLocator.locate());
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package org.jboss.shotoku.tags.tools;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class Constants {
    public static final String TAG_SERVICE_NAME = "shotoku:service=tag";

    public static final String SHOTOKU_TAG      = "shotoku";
    public static final String WEBSITE_TAG      = "website";

    public static final String TAG_FEED_TYPE_VARIABLE   = "%type%";

    /*
     * Property names, under which id and content manager directory can be
     * found, with which Shotoku tags -> Shotoku synchrozniation should be
     * done.
     */
    public static final String PROPERTY_CMID    = org.jboss.shotoku.tools.Constants.PROPERTIES_INTERNAL + ".tags.cmid";
    public static final String PROPERTY_CMDIR   = org.jboss.shotoku.tools.Constants.PROPERTIES_INTERNAL + ".tags.cmdir";

    /**
     * Property name, under which information if synchronization of tags
     * with shotoku should be done.
     */
    public static final String PROPERTY_SYNC   = org.jboss.shotoku.tools.Constants.PROPERTIES_INTERNAL + ".tags.synchronization";

    /**
     * Property name, under which the length of the interval between tags
     * service updates can be found.
     */
    public static final String PROPERTY_INTERVAL = org.jboss.shotoku.tools.Constants.PROPERTIES_INTERNAL + ".tags.service.interval";

    /**
     * Separator of tokens in a string representation of a tag.
     */
    public static final String SHOTOKU_TAG_REPR_SEPARATOR = "::";

    /**
     * A prefix of all shotoku property names, which are representations
     * of tags.
     */
    public static final String SHOTOKU_TAG_REPR_PREFIX      = "shotoku::tag::";
}

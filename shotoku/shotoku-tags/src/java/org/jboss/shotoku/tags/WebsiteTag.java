package org.jboss.shotoku.tags;

import org.jboss.shotoku.tags.tools.Constants;

import java.util.Date;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class WebsiteTag extends AbstractTag {
    public WebsiteTag(String name, String author, String resourceId,
                      String data, Date dateCreated) {
        super(name, author, resourceId, data, dateCreated);
    }

    public String getType() {
        return Constants.WEBSITE_TAG;
    }
}

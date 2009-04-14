package org.jboss.shotoku.tags;

import java.util.Date;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class DummyTag extends AbstractTag {
    private String type;

    public DummyTag(String name, String author, String resourceId,
                    String data, Date dateCreated, String type) {
        super(name, author, resourceId, data, dateCreated);

        this.type = type;
    }

    public String getType() {
        return type;
    }
}

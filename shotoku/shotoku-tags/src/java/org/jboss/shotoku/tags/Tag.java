package org.jboss.shotoku.tags;

import java.util.Date;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public interface Tag {
    public String getName();
    public String getAuthor();
    public String getResourceId();
    public String getData();
    public String getType();
    public Date getDateCreated();
    public Integer getFeedHits();
    public Integer getFeedVisits();
}

package org.jboss.shotoku.tags;

import org.jboss.shotoku.tags.tools.TagTools;
import org.jboss.shotoku.tags.tools.FeedType;

import java.util.Date;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class AbstractTag implements Tag {
	private String name;

	private String author;

	private String resourceId;

	private String data;

	private Date dateCreated;

    public AbstractTag(String name, String author, String resourceId,
			String data, Date dateCreated) {
		this.name = name;
		this.author = author;
		this.resourceId = resourceId;
		this.data = data;
		this.dateCreated = dateCreated;
    }

	/*
	 * TAG implementation.
	 */

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return author;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getData() {
		return data;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

    public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj.getClass().equals(this.getClass())) {
			AbstractTag otherTag = (AbstractTag) obj;

			if (name.equals(otherTag.getName())
					&& ((data == null && otherTag.getData() == null) || (data != null && data.equals(otherTag.getData())))
					&& resourceId.equals(otherTag.getResourceId())
					&& author.equals(otherTag.getAuthor())) {
				return true;
			}
		}

		return false;
	}

    public Integer getFeedHits() {
        return TagTools.getService().getFeedHits(FeedType.TAGS, getName());
    }

    public Integer getFeedVisits() {
        return TagTools.getService().getFeedVisits(FeedType.TAGS, getName());
    }

    public String toString() {
        return "(" + getName() + ", " + getAuthor() + ", " +
                getResourceId() + ")";
    }
}

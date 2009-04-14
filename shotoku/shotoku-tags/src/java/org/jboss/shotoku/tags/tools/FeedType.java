package org.jboss.shotoku.tags.tools;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public enum FeedType {
    /**
     * A feed of unique tags (unique tag names) on a given resource.
     */
    RESOURCE("resource"),
    /**
     * A feed of all tags with a given author.
     */
    AUTHOR_ALL("author"),
    /**
     * A feed of unique tags (unique tag names) with a given author.
     */
    AUTHOR_UNIQUE("author_tags"),
    /**
     * A feed of all tags with a given name and author.
     */
    AUTHOR_TAG("author_res"),
    TAGS("tags");

    private String strRep;

    FeedType(String strRep) {
        this.strRep = strRep;
    }

    public String toString() {
        return strRep;
    }
}

package org.jboss.shotoku.tags;

import org.jboss.shotoku.tags.exceptions.TagAddException;
import org.jboss.shotoku.tags.exceptions.TagDeleteException;
import org.jboss.shotoku.tags.exceptions.TagGetException;
import org.jboss.shotoku.tags.tools.FeedType;

import java.util.List;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public interface TagService {
    public void addTag(Tag t) throws TagAddException;
    public void deleteTag(Tag t) throws TagDeleteException;

    public Tag getTag(String tagName) throws TagGetException;

    /**
     * Gets all tags (that is, all tag name, author and resource ids
     * combinations) associated with the given names.
     * @param tagNames Names of the tags to get.
     * @return A list of appropriate tags implementations.
     * @throws TagGetException
     */
    public List<Tag> getTags(List<String> tagNames) throws TagGetException;

    /**
     * Gets all tags (that is, all tag name, author and resource ids
     * combinations) associated with the given resource.
     * @param resourceId Resouce id of the tags to get.
     * @return A list of appropriate tags implementations.
     * @throws TagGetException
     */
    public List<Tag> getTags(String resourceId) throws TagGetException;

    /**
     * Gets all tags (that is, all tag name, author and resource ids
     * combinations) associated with the given author.
     * @param author Author of the tags to get.
     * @return A list of appropriate tags implementations.
     * @throws TagGetException
     */
    public List<Tag> getTagsByAuthor(String author) throws TagGetException;

    /**
     * Gets unique tags (that is, all unique tag names)
     * associated with the given names.
     * @param tagNames Names of the tags to get.
     * @return A list of DummyTag implementatinos with the author and data
     * fields empty.
     * @throws TagGetException
     */
    public List<Tag> getUniqueTags(List<String> tagNames) throws TagGetException;

    /**
     * Gets unique tags (that is, all unique tag names and authors)
     * associated with the given author.
     * @param author Author of the tags to get.
     * @return A list of DummyTag implementations with the resource id
     * and data field empty.
     * @throws TagGetException
     */
    public List<Tag> getUniqueTagsByAuthor(String author) throws TagGetException;

    /**
     * Gets all tags (that is, all tag name, author and resource ids
     * combinations) associated with the given tag name and author.
     * @param tagName Name of tags to get.
     * @param author Author of tags to get.
     * @return A list of appropriate tags implementations.
     * @throws TagGetException
     */
    public List<Tag> getTags(String tagName, String author) throws TagGetException;

    /**
     * Gets unique tags (that is, all unique tag names)
     * associated with the given resource Id.
     * @param resourceId Resource id of the tags to get.
     * @return A list of DummyTag implementations with the author and
     * data field empty.
     * @throws TagGetException
     */
    public List<Tag> getUniqueTags(String resourceId) throws TagGetException;
    public List<Tag> getRelatedTags(List<Tag> relateTo) throws TagGetException;

    /**
     * Gets a link to a tag feed.
     * @param feedType Type of the tag feed - resource feed, author feed etc.
     * @param data Additinal data - resource id, author name etc.
     * @param type Type of the feed - rss2, atom or rdf.
     * @return A relative link to a feed with the given parameters.
     */
    public String getFeedLink(FeedType feedType, String data, String type);

    /**
     * Gets a link to a tag feed.
     * @param feedType Type of the tag feed - resource feed, author feed etc.
     * @param dataList Additional multiple data. This will be encoded into
     * a single string. For a author-tag feed, the first element should
     * be the name of tag, the second - author's name.
     * @param type Type of the feed - rss2, atom or rdf.
     * @return A relative link to a feed with the given parameters.
     */
    public String getFeedLink(FeedType feedType, List<String> dataList, String type);

    /**Gets all the tag names for given resourceId.
     * @param resourceId Resource ID
     * @return Tag list
     * @throws TagGetException
     */
    public List<String> getAllTagsNames(String resourceId) throws TagGetException;

    /**Gets all the tags
     * @return Tag list
     * @throws TagGetException
     */
    public List<Tag> getAllTags() throws TagGetException;

    /**
     * @param tag String query for tags
     * @param author String query for authors
     * @param keyword String query for keyword (searches thru description)
     * @return List of matching tags
     */
    public List<Tag> searchTags(String tag, String author, String keyword);

    /**
     * Gets count of number of times a feed of the given type and data
     * was opened. Data for feed types is: author name, resource id,
     * tag name or tag name '+' author name.
     * @param feedType Type of the feed to which the data corresponds to.
     * @param data Data of the feed for which the counter is read.
     * @return Number of times a given feed was opened.
     */
    public int getFeedHits(FeedType feedType, String data);

    /**
     * Gets count of number of times a feed of the given type and data
     * was opened by a unique IP address. Data for feed types is:
     * author name, resource id tag name or tag name '+' author name.
     * @param feedType Type of the feed to which the data corresponds to.
     * @param data Data of the feed for which the counter is read.
     * @return Number of times a given feed was opened by unique clients.
     */
    public int getFeedVisits(FeedType feedType, String data);

    /**
     * Increases the number of times a feed of the given type and data
     * was opened.
     * @param feedType Type of the feed for which to increase the counter.
     * @param data Data of the feed for which to increase the counter.
     * @param ipAddress IP address of the client that opened this feed.
     */
    public void increaseFeedCounters(FeedType feedType, String data,
                                    String ipAddress);
    
    /**Gets number of subscribers for all feeds to this author
     * @param author Author's login
     * @return Number of subscribers (visits)
     */
    public Integer getAllSubscribers(String author) throws TagGetException;
    
    public void create() throws Exception;

    public void start() throws Exception;

    public void stop();
    
    public void destroy();
    
    public void update();
}

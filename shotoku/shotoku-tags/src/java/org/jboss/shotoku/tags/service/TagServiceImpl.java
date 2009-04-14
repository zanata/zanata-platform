/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.shotoku.tags.service;

import java.util.*;

import javax.ejb.Local;
import javax.persistence.*;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.interceptor.Interceptors;

import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.ejb.Depends;
import org.jboss.shotoku.tools.Constants;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.tags.*;
import org.jboss.shotoku.tags.tools.FeedType;
import org.jboss.shotoku.tags.exceptions.TagGetException;
import org.jboss.shotoku.tags.exceptions.TagAddException;
import org.jboss.shotoku.tags.exceptions.TagDeleteException;
import org.jboss.shotoku.tags.dal.*;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.cache.CacheItem;
import org.jboss.shotoku.cache.CacheItemUser;
import org.jboss.shotoku.exceptions.SaveException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
@Service(objectName = org.jboss.shotoku.tags.tools.Constants.TAG_SERVICE_NAME)
@Local(TagServiceLocal.class)
@Management(TagService.class)
@Depends(Constants.SHOTOKU_SERVICE_NAME)
public class TagServiceImpl implements TagService, TagServiceLocal {
    private static final Logger log = Logger
            .getLogger(TagService.class);

    private boolean syncOn;

    private ContentManager cm;

    @PersistenceUnit
    private EntityManagerFactory emf;
    
    private CacheItemUser<Object, Object> tagci;

    /*
      * Service lifecycle management.
      */

    public void create() throws Exception {
    	tagci = CacheItem.create(new TagCacheItemDataSource(this));
    	
        tagci.setInterval(ContentManager.getProperty(
                org.jboss.shotoku.tags.tools.Constants.PROPERTY_INTERVAL,
                10000));

        syncOn = ContentManager.getProperty(
                org.jboss.shotoku.tags.tools.Constants.PROPERTY_SYNC, 0) == 1;
        if (syncOn) {
            cm = ContentManager.getContentManager(ContentManager.getProperty(
                    org.jboss.shotoku.tags.tools.Constants.PROPERTY_CMID,
                    Constants.DEFAULT_ID), ContentManager.getProperty(
                    org.jboss.shotoku.tags.tools.Constants.PROPERTY_CMDIR, ""));
        }
    }

    public void start() throws Exception {
    	tagci.get(new Object());
        log.info("Tag service started.");
    }

    public void stop() {
    	
    }
    
    public void destroy() {

    }

    /*
      * Timer-handling functions.
      */

    @SuppressWarnings("unchecked")
	public void update() {
        if (!syncOn) {
            // Synchronization is turned off.
            return;
        }

        EntityManager em = emf.createEntityManager();

        try {
            // Performing synchronization (tags -> shotoku).
            List<TagEntity> result = em.createQuery(
                    "FROM TagEntity " + "WHERE synced = 0 OR synced IS NULL")
                    .getResultList();

            if (result.size() == 0) {
                return;
            }

            Set<Resource> toSave = new HashSet<Resource>();
            for (TagEntity te : result) {
                if (org.jboss.shotoku.tags.tools.Constants.SHOTOKU_TAG
                        .equals(te.getType())) {
                    try {
                        Node n = cm.getNode(te.getResourceId());
                        n.setProperty(te.getShotokuPropReprName(), te
                                .getShotokuPropReprValue());
                        toSave.add(n);
                    } catch (ResourceDoesNotExist e) {
                        log
                                .warn(
                                        "Unable to synchronize tag with Shotoku resource, "
                                                + "the tagged resource does not exist.",
                                        e);
                    }
                }
            }

            try {
                cm.save("", toSave);
            } catch (SaveException e) {
                log.warn(e);
                return;
            }

            UserTransaction tran = null;
            try {
                InitialContext jndiCntx = new InitialContext();
                tran = (UserTransaction) jndiCntx.lookup("UserTransaction");

                tran.begin();
                em.joinTransaction();

                for (TagEntity te : result) {
                    te.setSynced(true);
                }

                tran.commit();
            } catch (Throwable t) {
                if (tran != null) {
                    try {
                        tran.rollback();
                    } catch (SystemException e) {
                        // Oh well ...
                    }
                }

                log.warn("Unable to synchronize tags.", t);
            }
        } finally {
            em.close();
        }
    }

    /*
      * TagService implementation.
      */

    /**
     * MIN_SIMILARITY - minimal similarity for checking relatedTags needed
     */
    private double MIN_SIMILARITY = 0.7d;

    private TagEntity getTagEntity(Tag t) {
        TagEntity te = new TagEntity();

        te.setAuthor(t.getAuthor());
        te.setData(t.getData());
        te.setDateCreated(t.getDateCreated());
        te.setName(t.getName());
        te.setResourceId(t.getResourceId());
        te.setType(t.getType());

        return te;
    }

    @Interceptors(ExceptionsInterceptor.class)
    public void addTag(Tag t) throws TagAddException {
        EntityManager manager = emf.createEntityManager();
        try {
            manager.persist(getTagEntity(t).normalizeName());
        } catch (Exception e) {
            throw new TagAddException(e);
        } finally {
            manager.close();
        }
    }

    public void deleteTag(Tag t) throws TagDeleteException {
        EntityManager manager = emf.createEntityManager();
        try {
            manager.remove(getTagEntity(t));
        } catch (Throwable e) {
            throw new TagDeleteException(e);
        } finally {
            manager.close();
        }
    }

    private void sortTagsByDate(List<Tag> tags) {
        Collections.sort(tags, new Comparator<Tag>() {
            public int compare(Tag o1, Tag o2) {
                return o1.getDateCreated().compareTo(o2.getDateCreated());
            }
        });
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getTags(String resourceId) throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            List<TagEntity> result = manager
                    .createQuery(
                            "from TagEntity where resourceId = :resourceId order by dateCreated desc")
                    .setParameter("resourceId", resourceId).getResultList();

            List<Tag> ret = new ArrayList<Tag>();
            for (TagEntity te : result) {
                ret.add(te.getTag());
            }

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    public Tag getTag(String tagName) throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            // noinspection unchecked
            TagEntity result = (TagEntity) manager.createQuery(
                    "from TagEntity where name = :name").setParameter("name",
                    tagName).getSingleResult();

            if (result == null) {
                return null;
            }

            return result.getTag();
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getTags(List<String> tagNames) throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            StringBuffer querySb = new StringBuffer("FROM TagEntity WHERE ");
            int i = 0;
            for (Iterator iter = tagNames.iterator(); iter.hasNext();) {
                querySb.append("name").append(" = :name").append(i);

                iter.next();

                if (iter.hasNext()) {
                    querySb.append(" OR ");
                }

                i++;
            }

            Query query = manager.createQuery(querySb.toString());
            i = 0;
            for (String tagName : tagNames) {
                query.setParameter("name" + i++, tagName);
            }

            List<TagEntity> result = query.getResultList();

            List<Tag> ret = new ArrayList<Tag>();
            for (TagEntity te : result) {
                ret.add(te.getTag());
            }

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getUniqueTags(List<String> tagNames)
            throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            StringBuffer querySb = new StringBuffer(
                    "SELECT name, resourceId, min(dateCreated), type "
                            + "FROM TagEntity WHERE ");
            int i = 0;
            for (Iterator iter = tagNames.iterator(); iter.hasNext();) {
                querySb.append("name").append(" = :name").append(i);

                iter.next();

                if (iter.hasNext()) {
                    querySb.append(" OR ");
                }

                i++;
            }

            querySb.append(" GROUP BY name, resourceId, type");

            Query query = manager.createQuery(querySb.toString());
            i = 0;
            for (String tagName : tagNames) {
                query.setParameter("name" + i++, tagName);
            }

            List<Object[]> result = query.getResultList();

            List<Tag> ret = new ArrayList<Tag>();
            for (Object[] o : result) {
                ret.add(new DummyTag((String) o[0], null, (String) o[1], null,
                        (Date) o[2], (String) o[3]));
            }

            sortTagsByDate(ret);

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings( { "unchecked" })
    public List<Tag> getRelatedTags(List<Tag> relateTo) throws TagGetException {
        List<Tag> ret = new ArrayList<Tag>();

        Map<String, List<Tag>> otherResources = new HashMap<String, List<Tag>>();

        EntityManager manager = emf.createEntityManager();
        try {
            for (Tag relatedTag : relateTo) {
                List<TagEntity> result = manager.createQuery(
                        "from TagEntity where name = :name").setParameter(
                        "name", relatedTag.getName()).getResultList();

                for (TagEntity otherTag : result) {
                    if (!otherResources.containsKey(otherTag.getResourceId())) {
                        otherResources.put(otherTag.getResourceId(),
                                getTags(otherTag.getResourceId()));
                    }
                }
            }

            for (List<Tag> tagList : otherResources.values()) {
                if (checkSimilarity(relateTo, tagList) >= MIN_SIMILARITY) {
                    ret.addAll(tagList);
                }
            }

            // don't return "relateTo" members
            List<Tag> endRet = new ArrayList<Tag>(ret);
            for (Tag tag : ret) {
                if (tagListContainsTag(tag, relateTo)) {
                    endRet.remove(tag);
                }
            }

            return endRet;
        } finally {
            manager.close();
        }
    }

    private boolean tagListContainsTag(Tag tag, List<Tag> listToCheck) {
        for (Tag tag2 : listToCheck) {
            if (tag.getName().equals(tag2.getName())) {
                return true;
            }
        }

        return false;
    }

    private double checkSimilarity(List<Tag> givenTags, List<Tag> listToCheck) {
        double ret = 0;

        for (Tag tag : givenTags) {
            if (tagListContainsTag(tag, listToCheck)) {
                ret++;
            }
        }

        return ret / (double) givenTags.size();
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getTagsByAuthor(String author) throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            List<TagEntity> result = manager
                    .createQuery(
                            "from TagEntity where author = :author order by dateCreated desc")
                    .setParameter("author", author).getResultList();

            List<Tag> ret = new ArrayList<Tag>();
            for (TagEntity te : result) {
                ret.add(te.getTag());
            }

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getUniqueTagsByAuthor(String author)
            throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            List<Object[]> result = manager.createQuery(
                    "SELECT name, min(dateCreated), type FROM TagEntity "
                            + "WHERE author =  :author " + "GROUP BY name, type") // +
                    // "ORDER BY mdc DESC")
                    .setParameter("author", author).getResultList();

            List<Tag> ret = new ArrayList<Tag>();
            for (Object[] o : result) {
                ret.add(new DummyTag((String) o[0], author, null, null,
                        (Date) o[1], (String) o[2]));
            }

            sortTagsByDate(ret);

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getTags(String name, String author) throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            // noinspection unchecked
            List<TagEntity> result = (List<TagEntity>) manager.createQuery(
                    "FROM TagEntity WHERE name = :name AND "
                            + "author = :author").setParameter("name", name)
                    .setParameter("author", author).getResultList();

            if (result == null) {
                return null;
            }

            List<Tag> ret = new ArrayList<Tag>();
            for (TagEntity te : result) {
                ret.add(te.getTag());
            }

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getUniqueTags(String resourceId) throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            List<Object[]> result = manager.createQuery(
                    "SELECT name, min(dateCreated), type FROM TagEntity "
                            + "WHERE resourceId = :resourceId "
                            + "GROUP BY name, type") // +
                    // "ORDER BY min(dateCreated) DESC")
                    .setParameter("resourceId", resourceId).getResultList();

            List<Tag> ret = new ArrayList<Tag>();
            for (Object[] o : result) {
                ret.add(new DummyTag((String) o[0], null, resourceId, null,
                        (Date) o[1], (String) o[2]));
            }

            sortTagsByDate(ret);

            return ret;
        } catch (Throwable e) {
            throw new TagGetException(e);
        } finally {
            manager.close();
        }
    }

    public String getFeedLink(FeedType feedType, String data, String type) {
        return "/feeds/tag/" + feedType.toString() + "/"
                + Tools.encodeURL(data) + "/" + type;
    }

    public String getFeedLink(FeedType feedType, List<String> dataList,
                              String type) {
        StringBuffer sb = new StringBuffer();

        for (Iterator<String> iter = dataList.iterator(); iter.hasNext();) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append("+");
            }
        }

        return "/feeds/tag/" + feedType.toString() + "/"
                + Tools.encodeURL(sb.toString()) + "/" + type;
    }

    @SuppressWarnings("unchecked")
	public List<String> getAllTagsNames(String resourceId)
            throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        return (List<String>) manager
                .createQuery(
                        "SELECT DISTINCT name FROM TagEntity WHERE resourceId = :resourceId")
                .setParameter("resourceId", resourceId).getResultList();
    }

    @SuppressWarnings("unchecked")
	public List<Tag> getAllTags() throws TagGetException {
        EntityManager manager = emf.createEntityManager();
        try {
            List<Tag> result = new ArrayList<Tag>();
            List<TagEntity> tagEnitites = manager.createQuery("from TagEntity")
                    .getResultList();

            for (TagEntity tagEnt : tagEnitites) {
                result.add(tagEnt.getTag());
            }

            return result;
        } finally {
            manager.close();
        }
    }

    public List<Tag> searchTags(String tag, String author, String keyword) {
        List<Tag> searchResult = new ArrayList<Tag>();

        log.debug("'" + tag + "' '" + author + "' '" + keyword + "'");

        TagSearch tagSearch = new TagSearch(tag, author, keyword);

        try {
            List<Tag> allTags = getAllTags();

            for (Tag nextTag : allTags) {
                if (tagSearch.matches(nextTag)) {
                    searchResult.add(nextTag);
                }
            }
        } catch (TagGetException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    public int getFeedHits(FeedType feedType, String data) {
        EntityManager manager = emf.createEntityManager();
        try {
            HitsEntity ce = manager.find(HitsEntity.class, new HitsIdEntity(
                    data, feedType.toString()));

            return ce == null ? 0 : ce.getCount();
        } finally {
            manager.close();
        }
    }

    public int getFeedVisits(FeedType feedType, String data) {
        EntityManager manager = emf.createEntityManager();
        try {
            VisitsEntity ve = manager.find(VisitsEntity.class,
                    new VisitsIdEntity(data, feedType.toString()));

            return ve == null ? 0 : ve.getCount();
        } finally {
            manager.close();
        }
    }

    public void increaseFeedCounters(FeedType feedType, String data,
                                     String ipAddress) {
        EntityManager manager = emf.createEntityManager();
        try {
            // 1. Increasing the hit counter.
            HitsEntity ce = manager.find(HitsEntity.class, new HitsIdEntity(
                    data, feedType.toString()));

            if (ce == null) {
                manager.persist(new HitsEntity(new HitsIdEntity(data, feedType
                        .toString()), 1));
            } else {
                ce.setCount(ce.getCount() + 1);
                manager.flush();
            }

            // 2. Increasing the visits counter.
            VisitsIpsEntity dbVie = manager
                    .find(VisitsIpsEntity.class, new VisitsIpsIdEntity(data,
                            feedType.toString(), ipAddress));

            if (dbVie == null) {
                // Storing the new ip address.
                try {
                    manager.persist(new VisitsIpsEntity(data, feedType
                            .toString(), ipAddress));
                } catch (Exception e) {
                    // Somebody must have already added it.
                    return;
                }

                // Increasing the counter.
                VisitsEntity ve = manager.find(VisitsEntity.class,
                        new VisitsIdEntity(data, feedType.toString()));

                if (ve == null) {
                    manager.persist(new VisitsEntity(new VisitsIdEntity(data,
                            feedType.toString()), 1));
                } else {
                    ve.setCount(ve.getCount() + 1);
                    manager.flush();
                }
            }
        } finally {
            manager.close();
        }
    }

    public Integer getAllSubscribers(String author) throws TagGetException {
        Integer visits = 0;

        List<Tag> tagsByAuthor = getTagsByAuthor(author);

        for (Tag tag : tagsByAuthor) {
            visits += getFeedVisits(FeedType.AUTHOR_TAG, tag.getName() + "+"
                    + tag.getAuthor());
        }

        visits += getFeedVisits(FeedType.AUTHOR_ALL, author);
        visits += getFeedVisits(FeedType.AUTHOR_UNIQUE, author);

        return visits;
    }
}

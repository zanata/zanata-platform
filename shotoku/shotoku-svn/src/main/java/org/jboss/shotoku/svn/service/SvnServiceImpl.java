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
package org.jboss.shotoku.svn.service;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import javax.ejb.Local;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.ejb.Depends;
//import org.jboss.shotoku.cache.CacheItem;
//import org.jboss.shotoku.cache.CacheItemUser;
import org.jboss.shotoku.svn.SvnService;
import org.jboss.shotoku.svn.SvnTools;
import org.jboss.shotoku.svn.SvnContentManager;
import org.jboss.shotoku.svn.service.delayed.DelayedOperation;
import org.jboss.shotoku.tools.Constants;
import org.jboss.shotoku.ContentManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
@Service(objectName=SvnTools.SVN_SERVICE_NAME)
@Local(SvnServiceLocal.class)
@Management(SvnService.class)
@Depends(Constants.SHOTOKU_SERVICE_NAME)
public class SvnServiceImpl implements SvnService, SvnServiceLocal {
    private static final Logger log = Logger.getLogger(SvnService.class);

    private ConcurrentMap<String, SvnRepository> repositories;

    private boolean firstUpdate;

//    private CacheItemUser<Object, Object> svnCacheItem;
    
    /*
     * Service lifecycle management.
     */

    public void create() throws Exception {
        // Set up connection protocols support:
        // for DAV (over http and https)
        DAVRepositoryFactory.setup();
        log.info("DAVRepositoryFactory setup.  Setting up SVNRepositoryFactory...");

        // for SVN (over svn and svn+ssh)
        SVNRepositoryFactoryImpl.setup();
        log.info("SVNRepositoryFactory setup complete.");

        repositories = new ConcurrentHashMap<String, SvnRepository>();

//        svnCacheItem = CacheItem.create(new SvnCacheItemDataSource(this));
//        svnCacheItem.get(new Object());
//        svnCacheItem.setInterval(ContentManager.getProperty(
//                SvnTools.PROPERTY_INTERVAL, SvnTools.DEFAULT_TIMER_INTERVAL));
        setFirstUpdate(ContentManager.getProperty(
                SvnTools.FIRST_UPDATE, 1) != 0);

        ContentManager.initializeContentManager(SvnContentManager.class.getName());
    }

    public void start() throws Exception {
        log.info("Subversion Service started.");
    }
    
    public void stop() {
    	
    }

    public void destroy() {
    }

    /*
      * Timer-handling functions.
      */

    public void update() {
        for (SvnRepository repo : repositories.values()) {
            try {
                repo.update();
            } catch (Exception e) {
                log.error("Exception while updating repository: " +
                        repo.getId() + ".", e);
            }
        }
    }

    public String getServiceInfo() {
        StringBuffer sb = new StringBuffer("These repositories are currently handled: ");
        for (String id : repositories.keySet()) {
            sb.append(id).append(" (").
                    append(repositories.get(id).getLastRevision()).append("); ");
        }

        return sb.append(".").toString();
    }

    /*
     * SvnService implementation.
     */

    public boolean getFirstUpdate() {
        return firstUpdate;
    }

    public void setFirstUpdate(boolean firstUpdate) {
        this.firstUpdate = firstUpdate;
    }

    public void registerRepository(String id, Configuration conf) {
        if (repositories.get(id) == null) {
            SvnRepository repo = new SvnRepository(id,
                    conf.getString(SvnTools.PROPERTY_USERNAME),
                    conf.getString(SvnTools.PROPERTY_PASSWORD),
                    conf.getString(SvnTools.PROPERTY_URL),
                    conf.getString(SvnTools.PROPERTY_LOCALPATH),
                    conf.getInt(SvnTools.PROPERTY_FULLUPDATE, 1),
                    conf.getBoolean(SvnTools.PROPERTY_EXTERNALS, true));

            repositories.put(id, repo);

            // First update/ checkout.
            if (firstUpdate) {
                repo.firstUpdate();
            }

            log.info("Added svn repository: " + id);
        }
    }

    public boolean isModified(String id, String fullPath) {
        return repositories.get(id).isModified(fullPath);
    }

    public void addToDeleted(String id, String fullPath) {
        repositories.get(id).addToDeleted(fullPath);
    }

    public boolean isDeleted(String id, String fullPath) {
        return repositories.get(id).isDeleted(fullPath);
    }

    public void addNodeToModfied(String id, String fullPath) {
        repositories.get(id).addNodeToModfied(fullPath);
    }

    public boolean isNodeModified(String id, String fullPath) {
        return repositories.get(id).isNodeModified(fullPath);
    }

    public void addTreeToModfied(String id, String fullPath) {
        repositories.get(id).addTreeToModfied(fullPath);
    }

    public boolean isTreeModified(String id, String fullPath) {
        return repositories.get(id).isTreeModified(fullPath);
    }

    public void addDirectoryToModfied(String id, String fullPath) {
        repositories.get(id).addDirectoryToModfied(fullPath);
    }

    public boolean isDirectoryModified(String id, String fullPath) {
        return repositories.get(id).isDirectoryModified(fullPath);
    }

    public void addDelayedOperation(String id, DelayedOperation op) {
        repositories.get(id).addDelayedOperation(op);
    }

    public void addPathsToUpdate(String id, long revision, Set<String> paths) {
        repositories.get(id).addPathsToUpdate(id, revision, paths);
    }
}

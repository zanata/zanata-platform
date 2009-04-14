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

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.tools.ConcurrentSet;
import org.jboss.shotoku.tools.ConcurrentHashSet;
import org.jboss.shotoku.tools.Pair;
import org.jboss.shotoku.svn.service.delayed.DelayedOperation;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * A class for performing operations on a single subversion repository and
 * its local working copy.
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnRepository {
    private static final Logger log = Logger.getLogger(SvnRepository.class);

    private SVNClientManager ourClientManager;
    private SVNRepository repository;
    private String url;
    private File wc;
    private ConcurrentSet<String> modifiedFiles;
    private ConcurrentSet<String> modifiedDirs;
    private ConcurrentSet<String> modifiedTrees;
    private ConcurrentSet<String> deletedResources;
    private ConcurrentSet<String> frozenModifiedFiles;
    private ConcurrentSet<String> frozenModifiedDirs;
    private ConcurrentSet<String> frozenModifiedTrees;
    private ConcurrentSet<String> frozenDeletedResources;

    /**
     * Number of updates after which a full WC update is done.
     */
    private int fullUpdate;
    /**
     * Number of updates since last full WC update.
     */
    private int updateCounter;

    /**
     * Should externals be checked out during a WC update.
     */
    private boolean ignoreExternals;

    private Set<Pair<Long, Set<String>>> toUpdate;

    /**
     * <code>lastRevision</code> - last revision to which the wc was updated.
     * Used to prevent unnecessary updates if the revision hasn't changed.
     */
    private long lastRevision;

    private ConcurrentSet<DelayedOperation> delayedOperations;
    private String id;
    private ContentManager cm;

    public SvnRepository(String id, String username, String password,
                         String url, String localpath, int fullUpdate,
                         boolean externals) {
        log.info("Creating new Subversion Repository:  id: "+id+
                ", username: "+username+
                ", passwd: "+password+
                ", url: "+url+
                ", localpath: "+localpath);
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager =
                SVNClientManager.newInstance(options, username, password);
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(
                    url));
            log.info("  SVNRepositoryFactory.create() call successful.");
            repository.setAuthenticationManager(
                    SVNWCUtil.createDefaultAuthenticationManager(username,
                            password));
        } catch (SVNException e) {
            log.warn("Error while creating SVNRepository.", e);
        }

        lastRevision = -1;

        this.url = url;
        this.id = id;

        this.fullUpdate = fullUpdate;
        this.ignoreExternals = !externals;
        updateCounter = 0;

        wc = new File(localpath);

        modifiedTrees = new ConcurrentHashSet<String>();
        modifiedFiles = new ConcurrentHashSet<String>();
        modifiedDirs = new ConcurrentHashSet<String>();
        deletedResources = new ConcurrentHashSet<String>();

        frozenModifiedTrees = new ConcurrentHashSet<String>();
        frozenModifiedFiles = new ConcurrentHashSet<String>();
        frozenModifiedDirs = new ConcurrentHashSet<String>();
        frozenDeletedResources = new ConcurrentHashSet<String>();

        delayedOperations = new ConcurrentHashSet<DelayedOperation>();

        toUpdate = new ConcurrentHashSet<Pair<Long, Set<String>>>();

        log.info("SVNRepository created.");
    }

    /**
     * Gets the number of the last revision to which the WC was updated to.
     */
    public long getLastRevision() {
        return lastRevision;
    }

    public String getId() {
        return id;
    }

    /**
     * Tries to perform a cleanup on the local working copy. Called in case
     * of exceptions that occure while performing other operations.
     */
    private void tryCleanup() {
        try {
            ourClientManager.getWCClient().doCleanup(wc);
        } catch (Exception e) {
            debug("Exception while cleaning up: " + e.getMessage() + ".");
            // Nothing we can do.
        }
    }

    /**
     * Updates the local working copy, additionaly attempting a checkout if
     * update fails.
     */
    public void firstUpdate() {
        log.info("Updating the working copy for the first time...");
        update(true, true);
        log.info("First update complete.");
    }

    /**
     * Updates the local working copy.
     */
    public void update() {
        updateCounter++;
        if (updateCounter == fullUpdate) {
            // Everything will be updated anyway.
            toUpdate.clear();
            update(false, true);

            updateCounter = 0;
        } else {
            // Only updating the "toUpdate" resources.
            updateToUpdate();
        }
    }

    private void updateToUpdate() {
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(ignoreExternals);

        for (Iterator<Pair<Long, Set<String>>> iter = toUpdate.iterator(); iter.hasNext();) {
            Pair<Long, Set<String>> updateData = iter.next();
            // Only updating if the data is newer then the current one in the wc.
            if (updateData.getFirst() > lastRevision) {
                for (String path : updateData.getSecond()) {
                    File wcPath = new File(wc.getPath() + File.separator + path);
                    try {
                        updateClient.doUpdate(wcPath, SVNRevision.HEAD, true);
                    } catch (SVNException e) {
                        log.warn("Error updating a modified path: " + path + ".", e);
                    }
                }
            }

            iter.remove();
        }
    }

    private void debug(String message) {
        log.debug(message);
    }

    /**
     * Updates the repository. If it fails, and <code>tryCheckout</code> is
     * set to true, a checkout is attempted; if it set to false, and
     * <code>firstTime</code> is set to true, then we are trying to do a
     * cleanup on the WC and execute this method once again, but this time with
     * <code>firstTime</code> set to false, to prevent a loop.
     *
     * @param tryCheckout
     *            True iff a checkout is to be attempted on update failure.
     * @param firstTime
     *            True iff this is the first attempt to do an update.
     */
    private synchronized void update(boolean tryCheckout, boolean firstTime) {
        debug("Starting SVN update...");
        /*
         * Checking if there is any need for updating the working copy.
         */
        try {
            if (lastRevision == repository.getLatestRevision()) {
                debug("No update necessary, performing delayed operations...");
                performDelayedOperations();
                debug("Ending SVN update.");
                return;
            }
        } catch (SVNException e) {
            debug("Exception when checking if working copy update is needed: " +
                    e.getMessage());
            // Trying to continue ...
        }

        /*
         * Freezing all entries in the modified/ deleted sets. After update,
         * these sets will be cleaned, because all changes that they stored
         * information about will be reflected in the WC. But any files that
         * are modified/ deleted during the update, have to be stored in
         * separate sets, and can't be removed from them after the update.
         */
        frozenModifiedFiles.addAll(modifiedFiles);
        modifiedFiles.clear();

        frozenModifiedDirs.addAll(modifiedDirs);
        modifiedDirs.clear();

        frozenModifiedTrees.addAll(modifiedTrees);
        modifiedTrees.clear();

        frozenDeletedResources.addAll(deletedResources);
        deletedResources.clear();

        // Doing the actual update.
        try {
            SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
            updateClient.setIgnoreExternals(ignoreExternals);
            try {
                debug("Updating repository: " + getId() +
                        "; revision number before update: " +
                        getLastRevision() + ".");
                /*
                 * Trying to do an update - if it does not succeed, then most
                 * probably the wc hasn't been checked out yet. That's why
                 * we try to do a check out in case of an error.
                 */
                lastRevision = updateClient.doUpdate(wc, SVNRevision.HEAD,
                        true);
                debug("Finished updating repository: " + getId() +
                        "; revision number after update: " +
                        getLastRevision() + ".");
            } catch (SVNException e) {
                SVNURL repositoryURL = SVNURL.parseURIEncoded(url);

                if (tryCheckout) {
                    debug("Exception when updating: " + e.getErrorMessage() + ", trying " +
                        "to perform a checkout.");
                    lastRevision = updateClient.doCheckout(repositoryURL, wc,
                            SVNRevision.HEAD, SVNRevision.HEAD, true);
                    debug("Checked out revision " + lastRevision + ".");
                } else {
                    debug("Exception when updating: " + e.getErrorMessage() + ", not " +
                            "trying to perform a checkout.");
                    throw e;
                }
            }
        } catch (SVNException e) {
            if (firstTime) {
                debug("First-time update threw an exception: " + e.getMessage() +
                        ", calling cleanup ...");
                tryCleanup();
                debug("First-time update threw an exception, calling update again...");
                update(tryCheckout, false);
            } else {
                log.error("Error while updating the repository.", e);
            }
        }

        // Clearing the just updated files.
        frozenDeletedResources.clear();
        frozenModifiedFiles.clear();
        frozenModifiedDirs.clear();
        frozenModifiedTrees.clear();

        // Finally, executing any delayed operations (no point in doing this
        // in a first-time update).
        if (!firstTime) {
            debug("Performing delayed operations...");
            performDelayedOperations();
        }

        debug("Ending SVN update.");
    }

    public void addPathsToUpdate(String id, long revision, Set<String> paths) {
        toUpdate.add(new Pair<Long, Set<String>>(revision, paths));
    }

    private void deleteFromSet(Set<String> resources, String deletedPath) {
        for (Iterator<String> iter = resources.iterator(); iter.hasNext();) {
            if (iter.next().startsWith(deletedPath)) {
                iter.remove();
            }
        }
    }

    public void addToDeleted(String fullPath) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        /*
         * From the modified sets, we have to remove every path that is a child
         * of the deleted one.
         */
        deleteFromSet(modifiedFiles, suffFullPath);
        deleteFromSet(modifiedDirs, suffFullPath);
        deleteFromSet(modifiedTrees, suffFullPath);
        deleteFromSet(frozenModifiedFiles, suffFullPath);
        deleteFromSet(frozenModifiedDirs, suffFullPath);
        deleteFromSet(frozenModifiedTrees, suffFullPath);

        deletedResources.add(suffFullPath);
    }

    private boolean checkPrefixes(Set<String> resources, String path) {
        for (String prefix : resources) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    public boolean isDeleted(String fullPath) {
        /*
           * Checking if a path in the delete resources set is a prefix of the
           * given one. If so, it means that a directory was deleted containing
           * the given path, so the path itself is deleted too. There is one
           * edge case, when there is a path in the set that is a prefix of the
           * given path but is not a directory - in that case the check could
           * return incorrect results. That's why every path is suffixed with a
           * /.
           */

        String suffFullPath = Tools.addPaths(fullPath, "");
        return checkPrefixes(deletedResources, suffFullPath) ||
                checkPrefixes(frozenDeletedResources, suffFullPath);
    }

    public boolean isModified(String fullPath) {
        return isNodeModified(fullPath, false) ||
                isDirectoryModified(fullPath, false) ||
                isTreeModified(fullPath);
    }

    public void addNodeToModfied(String fullPath) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        /*
           * We don't delete the modified path from the "deleted" dirty-set,
           * because it also represents information that children of that
           * path are also deleted. So, we remember information about which
           * path is created anew (in case it is in the delted set also)
           * and always check in the modified set before checking the
           * deleted set.
           */

        modifiedFiles.add(suffFullPath);
    }

    public boolean isNodeModified(String fullPath) {
        return isNodeModified(fullPath, true);
    }

    public boolean isNodeModified(String fullPath, boolean checkTree) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        return modifiedFiles.contains(suffFullPath) ||
                frozenModifiedFiles.contains(suffFullPath) ||
                (checkTree && isTreeModified(fullPath));
    }

    public void addDirectoryToModfied(String fullPath) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        modifiedDirs.add(suffFullPath);
    }

    public boolean isDirectoryModified(String fullPath) {
        return isDirectoryModified(fullPath, true);
    }

    public boolean isDirectoryModified(String fullPath, boolean checkTree) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        return modifiedDirs.contains(suffFullPath) ||
                frozenModifiedDirs.contains(suffFullPath) ||
                (checkTree && isTreeModified(fullPath));
    }

    public void addTreeToModfied(String fullPath) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        modifiedTrees.add(suffFullPath);
    }

    public boolean isTreeModified(String fullPath) {
        String suffFullPath = Tools.addPaths(fullPath, "");

        return checkPrefixes(modifiedTrees, suffFullPath) ||
                checkPrefixes(frozenModifiedTrees, suffFullPath);
    }

    public void addDelayedOperation(DelayedOperation op) {
        delayedOperations.add(op);
    }

    private void performDelayedOperations() {
        if (cm == null) {
            // Initializing if necessary.
            cm = ContentManager.getContentManager(id, "");
        }

        for (Iterator<DelayedOperation> it = delayedOperations.iterator(); it.hasNext();) {
            DelayedOperation op = it.next();

            try {
                op.perform(cm);
            } catch (ResourceDoesNotExist e) {
                log.warn("Delayed operation was supposed to be executed on a non-existent" +
                        "resource " + op.getClass().getName(), e);
            } catch (Exception e) {
                log.error("Error while performing a delayed operation " +
                        op.getClass().getName(), e);
            }

            it.remove();
        }
    }
}

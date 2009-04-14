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
package org.jboss.shotoku.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;
import org.jboss.shotoku.svn.operations.PathsStack;
import org.jboss.shotoku.svn.operations.ResourceOperation;
import org.jboss.shotoku.svn.operations.WorkspaceMediator;
import org.jboss.shotoku.tools.Tools;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.*;

/**
 * An implementation of the content manager based on subversion.
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnContentManager extends ContentManager {
    /**
     * <code>service</code> - service interface binding.
     */
    private static SvnService service;

    public static void setup(String id, Configuration conf) {
        service.registerRepository(id, conf);
    }

    public static void setup() {
        service = SvnTools.getService();

        // Configuring the repositories.
        // for DAV (over http and https)
        DAVRepositoryFactory.setup();

        // for SVN (over svn and svn+ssh)
        SVNRepositoryFactoryImpl.setup();
    }

    public static SvnService getService() {
        return service;
    }

    /*
     * INITIALIZATION AND HELPER METHODS
     */

    private int prefixLength;
    private ISVNRepositoryPool repoPool;
    private ThreadLocal<SVNClientManager> tlClientManager;
    private String localPath;
    private SVNURL svnUrl;

    public SvnContentManager(String id, String prefix, Configuration conf)
            throws RepositoryException {
        super(id, prefix);

        prefixLength = prefix.length();

        final String username = conf.getString(SvnTools.PROPERTY_USERNAME);
        final String password = conf.getString(SvnTools.PROPERTY_PASSWORD);
        localPath = conf.getString(SvnTools.PROPERTY_LOCALPATH);

        // Creating a new repository.
        try {
            svnUrl = SVNURL.parseURIEncoded(conf.getString(SvnTools.PROPERTY_URL));
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }

        repoPool = new DefaultSVNRepositoryPool(
                SVNWCUtil.createDefaultAuthenticationManager(username, password),
                SVNWCUtil.createDefaultOptions(false),
                true, DefaultSVNRepositoryPool.INSTANCE_POOL);

        tlClientManager = new ThreadLocal<SVNClientManager>() {
            protected SVNClientManager initialValue() {
                return SVNClientManager.newInstance(
                        SVNWCUtil.createDefaultOptions(true),
                        username, password);
            }
        };
    }

    /**
     * Gets a full repository path for the given path - that is, adds a prefix
     * this content manager's prefix.
     * @param path Path to this resource.
     * @return A prefixed path to the given resource.
     */
    private String getPrefixedPath(String path) {
        path = path.trim();
        if ("".equals(path)) return getPrefix();
        return Tools.normalizeSlashes(Tools.concatenatePaths(getPrefix(), path), true);
    }

    private String replaceSeparators(String path) {
        return '/' == File.separatorChar ?
                path : path.replace('/', File.separatorChar);
    }

    /**
     * Gets a <code>java.io.File</code> object that corresponds to a resource
     * that, in the repository, can be found under the given path.
     * @param path Path to the resource in the repository, already prefixed.
     * @return A <code>java.io.File</code> object for the given path.
     */
    File getFileForPath(String path) {
        return new File(replaceSeparators(localPath + "/" + path));
    }

    /**
     * From the given full file path, extractes a node's name and full name.
     * Also, modifies the given path to a most compact form (removing any
     * unnecessary / and spaces.
     * @param path Path from which to extract the names.
     * @return An array of strings, containing 3 elements: { compacted full
     * file path, name of resource, full name of resource }.
     */
    String[] getNamesFromPath(String path) {
        // Compacting path, that is, throwing out unnecessary /.
        path = Tools.normalizeSlashes(path, true);

        // Constructing fullName - just getting rid of the prefix and possible
        // / on the first position.
        String fullName = path.trim().substring(prefixLength);
        if (fullName.startsWith("/"))
            fullName = fullName.substring(1);

        // Constructing name.
        String name;

        // Getting the index of last /.
        int lastSlash = fullName.lastIndexOf('/');

        if (lastSlash != -1) {
            name = fullName.substring(lastSlash+1);
        } else
            name = fullName;

        return new String[] { path, name, fullName };
    }

    SVNRepository getRepository() throws SVNException {
        return repoPool.createRepository(svnUrl, true);
    }

    SVNClientManager getClientManager() {
        return tlClientManager.get();
    }

    enum ExistsType {
        DOES_NOT_EXIST,
        NODE,
        DIRECTORY,
        ANY
    }

    ExistsType checkIfExists(String fullPath) {
        return checkIfExists(fullPath, ExistsType.ANY);
    }

    ExistsType checkIfExists(String fullPath, ExistsType wantedType) {
        if (wantedType != ExistsType.DIRECTORY &&
                service.isNodeModified(getId(), fullPath)) {
            return ExistsType.NODE;
        }

        if (wantedType != ExistsType.NODE &&
                service.isDirectoryModified(getId(), fullPath)) {
            return ExistsType.DIRECTORY;
        }

        if (service.isDeleted(getId(), fullPath)) {
            return ExistsType.DOES_NOT_EXIST;
        }

        File file = getFileForPath(fullPath);

        if (!file.exists()) {
            return ExistsType.DOES_NOT_EXIST;
        }

        if (wantedType != ExistsType.DIRECTORY && file.isFile()) {
            return ExistsType.NODE;
        }

        if (wantedType != ExistsType.NODE && file.isDirectory()) {
            return ExistsType.DIRECTORY;
        }

        return ExistsType.DOES_NOT_EXIST;
    }

    /**
     * Checks that the given string is not null. If it is, an exception is
     * thrown with a custom message.
     * @param name Name to check.
     */
    void checkNotNull(String name) {
        if (name == null) {
            throw new RepositoryException("Path cannot be null!");
        }
    }

    /*
      * ContentManager IMPLEMENTATION
      */

    @Override
    public SvnDirectory getRootDirectory() {
        try {
            return getDirectory("");
        } catch (ResourceDoesNotExist e) {
            // Impossible.
            return null;
        }
    }

    @Override
    public SvnNode getNode(String path) throws ResourceDoesNotExist {
        checkNotNull(path);

        String prefixedPath = getPrefixedPath(path);

        if (checkIfExists(prefixedPath, ExistsType.NODE) != ExistsType.NODE)
            throw new ResourceDoesNotExist(prefixedPath);

        return new SvnNodeProxy(getId(), prefixedPath, this);
    }

    @Override
    public SvnDirectory getDirectory(String path) throws ResourceDoesNotExist {
        if (path == null) {
            throw new NullPointerException("Path cannot be null!");
        }

        String prefixedPath = getPrefixedPath(path);

        if (checkIfExists(prefixedPath, ExistsType.DIRECTORY) !=
                ExistsType.DIRECTORY)
            throw new ResourceDoesNotExist(prefixedPath);

        return new SvnDirectoryProxy(getId(), prefixedPath, this);
    }

    @Override
    public void delete(Collection<Resource> resources)
            throws DeleteException {
        /*
           * Collection all operations to perform - duplicates are automatically
           * removed as we use a Set.
           */
        Set<ResourceOperation> ops = new HashSet<ResourceOperation>();
        for (Resource resource : resources) {
            SvnResource svnResource = (SvnResource) resource;
            svnResource.addDeleteOperation(ops);
        }

        // Sorting all operations.
        List<ResourceOperation> opsList =
                new LinkedList<ResourceOperation>(ops);
        Collections.sort(opsList);

        // Now deleting any operations that are enclosed by other operations.
        if (opsList.size() >= 2) {
            ResourceOperation current = null;

            for (Iterator<ResourceOperation> iter = opsList.iterator();
                 iter.hasNext();) {
                if (current == null) {
                    // First operation - this hasn't to be deleted for sure.
                    current = iter.next();
                } else {
                    ResourceOperation ro = iter.next();

                    if (current.encloses(ro)) {
                        iter.remove();
                    } else {
                        current = ro;
                    }
                }
            }
        }

        try {
            performOperations(opsList, "");
        } catch (SVNException e) {
            throw new DeleteException(e);
        }
    }

    /**
     * A class for storing an empty-prefix content manager with a given
     * id, which is initialized on first demand.
     */
    public class ContentManagerHolder {
        private String id;
        private ContentManager cm;

        public ContentManagerHolder(String id) {
            this.id = id;
        }

        public ContentManager getContentManger() {
            if (cm == null) {
                cm = ContentManager.getContentManager(id, "");
            }

            return cm;
        }
    }

    @Override
    public void save(String logMessage, Collection<Resource> resources)
            throws SaveException {
        /*
         * Collection all operations to perform - duplicates are automatically
         * removed as we use a Set.
         */
        Set<ResourceOperation> ops = new HashSet<ResourceOperation>();
        Set<String> largePaths = new HashSet<String>();
        ContentManagerHolder cmh = new ContentManagerHolder(getId());
        for (Resource resource : resources) {
            SvnResource svnResource = (SvnResource) resource;
            svnResource.addOperations(ops, cmh);
            svnResource.addLargePaths(largePaths);
        }

        // Sorting all operations.
        List<ResourceOperation> opsList =
                new ArrayList<ResourceOperation>(ops);
        Collections.sort(opsList);

        try {
            performOperations(opsList, logMessage,
                    new WorkspaceMediator(largePaths));
        } catch (SVNException e) {
            throw new SaveException(e);
        }

        /*
           * Now notifying each resource that a save happened, so they can switch
           * their implementations (proxies) to the "repository" version.
           */
        for (Resource resource : resources) {
            try {
                SvnResource svnResource = (SvnResource) resource;
                svnResource.notifySaved();
            } catch (RepositoryException e) {
                throw new SaveException(e);
            }
        }
    }

    protected void performOperations(List<ResourceOperation> opsList,
                                     String logMessage) throws SVNException {
        performOperations(opsList, logMessage, new WorkspaceMediator(
                new HashSet<String>()));
    }

    private void abortEditor(ISVNEditor editor) {
        try {
            if (editor != null) {
                editor.abortEdit();
            }
        } catch (SVNException e1) {
            // Can't do much here.
        }
    }

    protected void performOperations(List<ResourceOperation> opsList,
                                     String logMessage, WorkspaceMediator mediator)
            throws SVNException {
        ISVNEditor editor = null;
        try {
            long lastRevision = getRepository().getLatestRevision();
            editor = getRepository().getCommitEditor(logMessage,
                    mediator);

            PathsStack stack = new PathsStack(editor);

            // Now that we have the good operation order, we can execute them.
            for (ResourceOperation op : opsList) {
                op.execute(stack, lastRevision);
            }

            // Closing the stack.
            stack.close();

            // Finally, adding paths to the modified sets.
            for (ResourceOperation op : opsList) {
                op.addModifiedPaths(service);
            }
        } catch (SVNException e) {
            abortEditor(editor);

            throw e;
        } catch (Exception e) {
            abortEditor(editor);

            throw new RepositoryException(e);
        }
    }

}

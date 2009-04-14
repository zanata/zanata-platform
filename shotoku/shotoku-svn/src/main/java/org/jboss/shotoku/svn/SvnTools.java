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
import java.io.OutputStream;
import java.util.*;

import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.svn.service.SvnServiceImpl;
import org.jboss.shotoku.tools.Constants;
import org.jboss.shotoku.tools.Pair;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.ISVNLocationEntryHandler;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Common methods used by the ShotokuSvn classes. 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnTools {
    /*
      * Name of properties in the configuration file - all should be prefixed
      * with "shotoku.<repository-id>.".
      */
    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_LOCALPATH = "localpath";
    public static final String PROPERTY_FULLUPDATE = "fullupdate";
    public static final String PROPERTY_EXTERNALS = "externals";

    /*
     * Names of internal shotoku properties.
     */
    public static final String INTERNAL_SVN_PROP_PREFIX = "svn:";
    public static final String INTERNAL_PROP_PREFIX = "shotoku:";
    public static final String INTERNAL_PROP_CREATED = INTERNAL_PROP_PREFIX + "created";

    /**
	 * <code>SVN_SERVICE_NAME</code> - name under which the svn service is
	 * registered.
	 */
	public final static String SVN_SERVICE_NAME = "shotoku:service=svn";

    /**
     * Default interval length of the
     * timer.
     */
    public final static long DEFAULT_TIMER_INTERVAL = 1000*5; // 5 seconds
    public static final String PROPERTY_INTERVAL = Constants.PROPERTIES_INTERNAL + ".svn.service.interval";

    /**
     * Property stating if first update should be executed on
     * repository registering. True by default.
     */
    public static final String FIRST_UPDATE = Constants.PROPERTIES_INTERNAL + ".svn.service.firstupdate";

    private static SvnService instance;

    /**
     * Gets an instance of Shotoku svn service - this should be always the same
     * the object, so the dirty sets work properly.
     * @return An instance of org.jboss.shotoku.svn.SvnService
     */
    public synchronized static SvnService getService() {
        try {
            if (instance == null) {
                if (ContentManager.isEmbedded()) {
                    // Embedded mode - simply creating a new service instance.
                    instance = new SvnServiceImpl();
                    instance.create();
                    instance.start();
                } else {
                    // Application server mode - creating a proxy to a mbean.
                    instance = (SvnService) MBeanProxyExt.create(
                            SvnService.class,
                            SVN_SERVICE_NAME,
                            MBeanServerLocator.locate());
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
      * COMMON SVN FUNCTIONS
      */

    public static String getProperty(SvnContentManager svnCm, File file,
                                     String name) {
        try {
            SVNPropertyData data = svnCm.getClientManager().getWCClient().
            	doGetProperty(file, name, SVNRevision.WORKING, SVNRevision.WORKING);
            return data == null ? null : SVNPropertyValue.getPropertyAsString(data.getValue());
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
    }

    public static String getLogMessageInternal(SvnContentManager svnCm,
                                               File file) {
        try {
            class LocalStringHolder { String content; }
            final LocalStringHolder ret = new LocalStringHolder();

            svnCm.getClientManager().getLogClient().doLog(
                    new File[] { file }, SVNRevision.WORKING,
                    SVNRevision.WORKING, false, false, 1,
                    new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry entry) {
                            ret.content = entry.getMessage();
                        }
                    });

            return ret.content;
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
    }

    public static void getNodeContent(String fullPath, long revision,
                                      SvnContentManager svnCm, OutputStream os,
                                      Map<String, String> properties) {
        getNodeContent(fullPath, revision, svnCm, os, properties, false, 0);
    }

    public static void getNodeContent(String fullPath, long revision,
                                      SvnContentManager svnCm, OutputStream os,
                                      Map<String, String> properties, boolean lookupLocation,
                                      long pegRevision) {
        try {
            final String[] path = new String[1];

            // First getting the location of the file (it may have changed)
            if (lookupLocation) {
                svnCm.getRepository().getLocations(fullPath.substring(1),
                        pegRevision, new long[] { revision },
                        new ISVNLocationEntryHandler() {
                            public void handleLocationEntry(SVNLocationEntry svnLocationEntry) {
                                path[0] = svnLocationEntry.getPath();
                            }
                        });
            } else {
                path[0] = fullPath.substring(1);
            }

            // For this operation the path cannot begin with a / - removing it.
            svnCm.getRepository().getFile(path[0], revision,
            		SVNProperties.wrap(properties), os);
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
    }

    public static void getDirectoryInfo(String fullPath, long revision,
                                        SvnContentManager svnCm, Map<String, String> properties,
                                        List<String> childNodes, List<String> childDirs) {
        List<SVNDirEntry> entries = new ArrayList<SVNDirEntry>();
        try {
            // For this operation the path cannot begin with a / - removing it.
            svnCm.getRepository().getDir(fullPath.substring(1), revision,
                    SVNProperties.wrap(properties), entries);
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }

        for (SVNDirEntry entry : entries) {
            if (entry.getKind().equals(SVNNodeKind.FILE)) {
                childNodes.add(entry.getName());
            } else if (entry.getKind().equals(SVNNodeKind.DIR)) {
                childDirs.add(entry.getName());
            }
        }
    }

    public static List<SvnRevisionInfo> getFirstRevisionInformation(
            String fullPath, SvnContentManager svnCm) {
        long currentRevision;
        try {
            currentRevision = svnCm.getRepository().getLatestRevision();
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
        return getRevisionInformation(fullPath, svnCm, 0,
                currentRevision, 1);
    }

    public static List<SvnRevisionInfo> getCurrentRevisionInformation(
            String fullPath, SvnContentManager svnCm) {
        long currentRevision;
        try {
            currentRevision = svnCm.getRepository().getLatestRevision();
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
        return getRevisionInformation(fullPath, svnCm, currentRevision,
                1, 1);
    }

    public static Pair<List<SvnRevisionInfo>, Long> getAllRevisionsInformation(
            String fullPath, SvnContentManager svnCm) {
        long currentRevision;
        try {
            currentRevision = svnCm.getRepository().getLatestRevision();
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
        return new Pair<List<SvnRevisionInfo>, Long>(
                getRevisionInformation(fullPath, svnCm, 0, currentRevision, 0),
                currentRevision);
    }

    private static List<SvnRevisionInfo> getRevisionInformation(String fullPath,
                                                                SvnContentManager svnCm, long startRevision, long endRevision,
                                                                long limit) {
        try {
            final List<SvnRevisionInfo> ret = new ArrayList<SvnRevisionInfo>();

            // For this operation the path cannot begin with a / - removing it.
            svnCm.getRepository().log(new String[] { fullPath.substring(1) },
                    startRevision, endRevision, false, false, limit,
                    new ISVNLogEntryHandler() {
                        public void handleLogEntry(SVNLogEntry entry) {
                            ret.add(new SvnRevisionInfo(entry.getDate(),
                                    entry.getMessage(), entry.getRevision()));
                        }
                    });

            return ret;
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }
    }

    public static Map<String, String> getAllProperties(File file,
                                                       SvnContentManager svnCm) {
        final Map<String, String> ret = new HashMap<String, String>();
        try {
            svnCm.getClientManager().getWCClient().doGetProperty(file, 
            		null, 
            		SVNRevision.WORKING, 
            		SVNRevision.WORKING, SVNDepth.INFINITY, 
                    new ISVNPropertyHandler() {
                        public void handleProperty(File f, SVNPropertyData data) {
                            String name = data.getName();
                            if (!filterProperty(name)) {
                                ret.put(name, SVNPropertyValue.getPropertyAsString(data.getValue()));
                            }
                        }

                        public void handleProperty(SVNURL url,
                                                   SVNPropertyData data) { }
                        public void handleProperty(long revision,
                                                   SVNPropertyData data) { }

                    },null);
        } catch (SVNException e) {
            throw new RepositoryException(e);
        }

        return ret;
    }

    public static String getParentFullName(SvnResource res) {
        // Checking if this isn't the root directory already.
        if ("".equals(res.getName()))
            return null;

        int lastSlash = res.getFullName().lastIndexOf('/');
        if (lastSlash == -1) {
            // No / at all in the path - this resource must be a child of the
            // root directory.
            return "";
        } else {
            // Getting the parent path.
            return res.getFullName().substring(0, lastSlash);
        }
    }

    /**
     * Checks if this property should be made available to the user
     * when using getProperties().
     * @param propName Property name to check.
     * @return True iff the property name denotes an internal
     * property, that shouldn't be made available to the user when
     * using getProperties().
     */
    public static boolean filterProperty(String propName) {
        return (propName.startsWith(INTERNAL_SVN_PROP_PREFIX)) ||
               (propName.startsWith(INTERNAL_PROP_PREFIX));
    }

    /**
     * Filters the given properties map, removing the ones that shouldn't
     * be shown to the user ("internal" properties).
     * @param properties Properties to filter.
     */
    public static void filterProperties(Map<String, String> properties) {
        for (Iterator<String> iter = properties.keySet().iterator();
             iter.hasNext();) {
            if (filterProperty(iter.next())) {
                iter.remove();
            }
        }
    }
}

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
package org.jboss.shotoku;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.SaveException;
import org.jboss.shotoku.search.Search;
import org.jboss.shotoku.tools.Constants;
import org.jboss.shotoku.tools.Pair;
import org.jboss.shotoku.tools.Tools;

import static org.jboss.shotoku.tools.Tuples.*;

/**
 * A base class which provides access to nodes and directories. <br />
 * <b>Warning:</b> all shotoku classes are not thread-safe.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public abstract class ContentManager {
    /**
     * Prefix of this content manager.
     */
    private String prefix;

    /**
     * Id of a corresponding repository.
     */
    private String id;

    /**
     * Gets a root directory that is represented by this content manager.
     *
     * @return A directory that is the root directory of this content manager.
     * @throws RepositoryException
     */
    public abstract Directory getRootDirectory() throws RepositoryException;

    /**
     * Gets the most recent version of a node which can be found under the given
     * path (to get nodes from subdirectories, use <code>/</code> to separate
     * them).
     *
     * @param path
     *            Path for which to get the node.
     * @return A node corresponding to the given path.
     * @throws ResourceDoesNotExist
     * @throws RepositoryException
     */
    public abstract Node getNode(String path) throws ResourceDoesNotExist,
            RepositoryException;

    /**
     * Gets a directory which can be found under the given path (to get nodes
     * from subdirectories, use <code>/</code> to separate them).
     *
     * @param path
     *            Path for which to get the directory.
     * @return A directory corresponding to the given path.
     * @throws ResourceDoesNotExist
     * @throws RepositoryException
     */
    public abstract Directory getDirectory(String path)
            throws ResourceDoesNotExist, RepositoryException;

    /**
     * Performs the given search in this content manager.
     *
     * @param search
     *            Search to perform.
     * @return A node list that is the result of the search.
     */
    public NodeList search(Search search) throws ResourceDoesNotExist {
        return search.perform(this);
    }

    /**
     * Gets the id of this repository.
     *
     * @return Id of this repository.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the prefix of this content manager.
     *
     * @return Prefix of this content manager.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Saves the given resources at one time. Recommended if you have multiple
     * resources to save.
     *
     * @param logMessage
     *            Log message to save with.
     * @param resources
     *            Resources to save.
     * @throws SaveException
     * @throws RepositoryException
     */
    public abstract void save(String logMessage, Collection<Resource> resources)
            throws SaveException, RepositoryException;

    /**
     * Deletes the given resources at one time. Recommended if you have multiple
     * resources to delete.
     *
     * @param resources
     *            Resources to delete.
     * @throws DeleteException
     * @throws RepositoryException
     */
    public abstract void delete(Collection<Resource> resources)
            throws DeleteException, RepositoryException;

    /**
     * Deletes the given resource. Equivalent to resource.delete().
     *
     * @param resource
     *            Resources to delete.
     * @throws DeleteException
     * @throws RepositoryException
     */
    public void delete(Resource resource) throws DeleteException,
            RepositoryException {
        List<Resource> toDelete = new ArrayList<Resource>();
        toDelete.add(resource);
        delete(toDelete);
    }

    /**
     * Saves the given resource. Equivalent to <code>res.save(String)</code>.
     *
     * @param res
     *            First resource to save.
     * @param logMessage
     *            Log message to save with.
     * @throws SaveException
     * @throws RepositoryException
     */
    public void save(String logMessage, Resource res) throws SaveException,
            RepositoryException {
        List<Resource> toSave = new ArrayList<Resource>();
        toSave.add(res);
        save(logMessage, toSave);
    }

    /**
     * Saves the given resources at one time. Recommended if you have multiple
     * resources to save.
     *
     * @param logMessage
     *            Log message to save with.
     * @param resources
     *            Any number of resources
     * @throws RepositoryException
     * @throws SaveException
     */
    public void save(String logMessage, Resource... resources)
            throws RepositoryException, SaveException {
        List<Resource> toSave = new ArrayList<Resource>();

        for (Resource resource : resources) {
            toSave.add(resource);
        }

        save(logMessage, toSave);
    }

    /**
     * Deletes the given resources at one time. Recommended if you have multiple
     * resources to delete.
     *
     * @param resources
     *            Any number of resources to delete
     * @throws DeleteException
     * @throws RepositoryException
     */
    public void delete(Resource... resources) throws DeleteException,
            RepositoryException {
        List<Resource> toDelete = new ArrayList<Resource>();

        for (Resource resource : resources) {
            toDelete.add(resource);
        }

        delete(toDelete);
    }

    /**
     * Saves the given resources at one time. Equivalent to
     * <code>save(Collection, String)</code>, where the collection is filled
     * with the two given resources.
     *
     * @param logMessage
     *            Log message to save with.
     * @param res1
     *            First resource to save.
     * @param res2
     *            Second resource to save.
     * @throws SaveException
     * @throws RepositoryException
     */
    public void save(String logMessage, Resource res1, Resource res2)
            throws SaveException, RepositoryException {
        List<Resource> toSave = new ArrayList<Resource>();
        toSave.add(res1);
        toSave.add(res2);
        save(logMessage, toSave);
    }

    /**
     * Saves the given resources at one time. Equivalent to
     * <code>save(Collection, String)</code>, where the collection is filled
     * with the three given resources.
     *
     * @param logMessage
     *            Log message to save with.
     * @param res1
     *            First resource to save.
     * @param res2
     *            Second resource to save.
     * @param res3
     *            Third resource to save.
     * @throws SaveException
     * @throws RepositoryException
     */
    public void save(String logMessage, Resource res1, Resource res2,
                     Resource res3) throws SaveException, RepositoryException {
        List<Resource> toSave = new ArrayList<Resource>();
        toSave.add(res1);
        toSave.add(res2);
        toSave.add(res3);
        save(logMessage, toSave);
    }

    /**
     * Saves the given resources at one time. Equivalent to
     * <code>save(Collection, String)</code>, where the collection is filled
     * with the four given resources.
     *
     * @param logMessage
     *            Log message to save with.
     * @param res1
     *            First resource to save.
     * @param res2
     *            Second resource to save.
     * @param res3
     *            Third resource to save.
     * @param res4
     *            Fourth resource to save.
     * @throws SaveException
     * @throws RepositoryException
     */
    public void save(String logMessage, Resource res1, Resource res2,
                     Resource res3, Resource res4) throws SaveException,
            RepositoryException {
        List<Resource> toSave = new ArrayList<Resource>();
        toSave.add(res1);
        toSave.add(res2);
        toSave.add(res3);
        toSave.add(res4);
        save(logMessage, toSave);
    }

    /**
     * Saves the given resources at one time. Equivalent to
     * <code>save(Collection, String)</code>, where the collection is filled
     * with the five given resources.
     *
     * @param logMessage
     *            Log message to save with.
     * @param res1
     *            First resource to save.
     * @param res2
     *            Second resource to save.
     * @param res3
     *            Third resource to save.
     * @param res4
     *            Fourth resource to save.
     * @param res5
     *            Fifth resource to save.
     * @throws SaveException
     * @throws RepositoryException
     */
    public void save(String logMessage, Resource res1, Resource res2,
                     Resource res3, Resource res4, Resource res5)
            throws SaveException, RepositoryException {
        List<Resource> toSave = new ArrayList<Resource>();
        toSave.add(res1);
        toSave.add(res2);
        toSave.add(res3);
        toSave.add(res4);
        toSave.add(res5);
        save(logMessage, toSave);
    }

    /**
     * Standard constructor.
     *
     * @param id
     *            Id of this content manager.
     * @param prefix
     *            Prefix of this content manager, as stated in properties.
     * @throws RepositoryException
     */
    protected ContentManager(String id, String prefix)
            throws RepositoryException {
        this.id = id;
        this.prefix = Tools.normalizeSlashes(prefix, true);
    }

    /*
      * Content manager loading.
      */

    private static final Logger log;

    /**
     * Id of the default content manager. May be overriden by a setting in the
     * xml configuration file.
     */
    private static String defaultId;

    /**
     * Size of the transfer buffer.
     */
    private static int transferBufferSize;

    /**
     * Map (content manager impl class name -> content manager constructor).
     */
    private static Map<String, Constructor> contentManagers;

    /**
     * Map (content manager id -> pair (content manager class name, content
     * manager configuration)
     */
    public static Map<String, Pair<String, Configuration>> contentManagersImplementations;

    /**
     * A set of content manager implementation classes names, which are
     * already initialized.
     */
    private static Set<String> initializedContentManagers;

    /**
     * A set of content manager ids, which are already initialized.
     */
    private static Set<String> initializedIds;

    /**
     * A map content manager implementation class name -> a set of
     * ids which use the given implementation.
     */
    private static Map<String, Set<String>> implementationsIds;

    /**
     * <code>props</code> - Shotoku properties, as read from the configuration
     * file.
     */
    private static PropertiesConfiguration props;

    /**
     * <code>setupDone</code> - a marker to execute setup only once, even if
     * the user does it many times by mistake.
     */
    private static boolean setupDone;

    static {
        // Setup was not yet executed for sure.
        setupDone = false;

        // When this class is loaded, always reading configuration first.
        try {
            props = new PropertiesConfiguration();

            props.load(ContentManager.class
                    .getResourceAsStream(Constants.PROPERTIES_FILE));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while loading content manager configuration: ", e);
        }

        log = Logger.getLogger(ContentManager.class);
    }

    /**
     * Checks if Shotoku is configured to run in embedded mode or not.
     *
     * @return True iff Shotoku is configured to run in embedded mode.
     */
    public static boolean isEmbedded() {
        return Tools.isTrue(getProperty(Constants.PROPERTIES_EMBEDDED));
    }

    /**
     * Setups the content managers before first used. In embedded mode, should
     * be always called once, before any usage of Shotoku classes. In app server
     * mode, should be never called by the user.
     */
    public static void setup() {
        synchronized (ContentManager.class) {
            if (setupDone) {
                return;
            }

            // Preventing setup to be executed twice.
            setupDone = true;
        }

        contentManagers = new HashMap<String, Constructor>();
        initializedContentManagers = new HashSet<String>();
        initializedIds = new HashSet<String>();
        implementationsIds = new HashMap<String, Set<String>>();
        contentManagersImplementations = new HashMap<String, Pair<String, Configuration>>();

        // Reading the default properties.
        defaultId = props.getString(Constants.PROPERTIES_ID_DEFAULT,
                Constants.DEFAULT_ID);
        transferBufferSize = props.getInt(
                Constants.PROPERTIES_TRANSFER_BUF_SIZE,
                Constants.DEFAULT_TRANSFER_BUF_SIZE);
        // Getting ids of defined content managers.
        String[] ids = props.getStringArray(Constants.PROPERTIES_IDS);

        for (String id : ids) {
            try {
                Configuration conf = props.subset(Tools.concatenateProperties(
                        Constants.PROPERTIES_PREFIX, id));

                String implementation = conf
                        .getString(Constants.PROPERTIES_IMPL_SUFFIX);

                contentManagersImplementations.put(id, tuple(implementation, conf));

                Set<String> implIds = implementationsIds.get(implementation);
                if (implIds == null) {
                    implIds = new HashSet<String>();
                    implementationsIds.put(implementation, implIds);
                }

                implIds.add(id);

                log.info("Added content manager: " + id + ", "
                        + implementation);
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("Error setting up content manager " + id + ".", e);
            }
        }

    }

    private synchronized static void initializeId(String id) {
        if (!initializedIds.contains(id)) {
            try {
                Pair<String, Configuration> ic =
                        contentManagersImplementations.get(id);

                // First initializing the whole implementation, if it hasn't
                // been done yet.
                initializeContentManager(ic.getFirst());

                Constructor constr = contentManagers.get(ic.getFirst());
                Class implClass = constr.getDeclaringClass();

                // Executing the setup function.
                try {
                    Method setupMethod = implClass.getMethod(
                            Constants.SETUP_FUNCTION, String.class,
                            Configuration.class);

                    setupMethod.invoke(implClass, id, ic.getSecond());
                } catch (NoSuchMethodException e) {
                    // No setup - doing nothing.
                }

                log.info("Initialized content manager: " + id + ", "
                        + implClass.getName());

                initializedIds.add(id);
            } catch (Exception e) {
                log.warn("Error initializing content manager: " + id + ".", e);
            }
        }
    }

    public synchronized static void initializeContentManager(String implementation) {
        if (!initializedContentManagers.contains(implementation)) {
            try {
                Class implClass = Thread.currentThread().getContextClassLoader().loadClass(implementation);

                // Executing the setup function.
                try {
                    Method setupMethod = implClass.getMethod(Constants.SETUP_FUNCTION);

                    setupMethod.invoke(implClass);
                } catch (NoSuchMethodException e) {
                    // No setup - doing nothing.
                }

                log.info("Initialized content manager implementation: " + implementation);

                initializedContentManagers.add(implementation);

                contentManagers.put(implementation,
                        implClass.getConstructor(String.class,
                                String.class, Configuration.class));

                // Initializing all ids.
                Set<String> implIds = implementationsIds.get(implementation);
                if (implIds != null) {
                    for (String id : implIds) {
                        initializeId(id);
                    }
                }
            } catch (Exception e) {
                log.warn("Error initializing content manager implementation: " +
                        implementation + ".", e);
            }
        }
    }


    /**
     * Gets a content manager with a default id and an empty prefix.
     *
     * @return A ""-prefixed, default content manager, or null, if a default
     *         content manager is not registered.
     */
    public static ContentManager getContentManager() {
        return getContentManager("");
    }

    /**
     * Gets a content manager with a default id and the given prefix.
     *
     * @param prefix
     *            Prefix for the new content manager (all paths will have this
     *            prepended).
     * @return A prefixed, default content manager, or null, if a default
     *         content manager is not registered.
     */
    public static ContentManager getContentManager(String prefix) {
        return getContentManager(defaultId, prefix);
    }

    /**
     * Gets a content manager with the given id and prefix.
     *
     * @param id
     *            Id of the content manager.
     * @param prefix
     *            Prefix for the new content manager (all paths will have this
     *            prepended).
     * @return A prefixed content manager with the given id or null, if a
     *         content manager with the given id registers.
     */
    public static ContentManager getContentManager(String id, String prefix) {
        if (isEmbedded()) {
            // Calling setup - in case this is the first use.
            setup();
        }

        if (id == null) {
            id = defaultId;
        }

        if (prefix == null) {
            prefix = "";
        } else {
            prefix = Tools.normalizeSlashes(prefix, true);
        }

        Pair<String, Configuration> cmData = contentManagersImplementations.get(id);

        if (cmData == null) {
            return null;
        }

        initializeId(id);

        Constructor constr = contentManagers.get(cmData.getFirst());

        try {
            return (ContentManager) constr.newInstance(id, prefix,
                    cmData.getSecond());
        } catch (Throwable e) {
            log.warn("Unable to get a content manager: (" + id + ", " + prefix
                    + ").", e);
            return null;
        }
    }

    /*
      * Properties accessors.
      */

    /**
     * Gets a property of this content manager, as it is written in the
     * configuration file (helper method).
     *
     * @param name
     *            Name of the property to get.
     * @return Value of the given property or null, if no such property exists.
     */
    public static String getProperty(String name) {
        return props.getString(name);
    }

    /**
     * Gets a property of this content manager, as it is written in the
     * configuration file (helper method for implementations). Only internal
     * properties! (long parameter value).
     *
     * @param name
     *            Name of the property to get.
     * @param defaultValue
     *            Value to return in case the given property is not set.
     * @return Value of the given property or null, if no such property exists.
     */
    public static long getProperty(String name, long defaultValue) {
        if (name.startsWith(Constants.PROPERTIES_INTERNAL)) {
            return props.getLong(name, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a property of this content manager, as it is written in the
     * configuration file (helper method for implementations). Only internal
     * properties! (int parameter value).
     *
     * @param name
     *            Name of the property to get.
     * @param defaultValue
     *            Value to return in case the given property is not set.
     * @return Value of the given property or null, if no such property exists.
     */
    public static int getProperty(String name, int defaultValue) {
        if (name.startsWith(Constants.PROPERTIES_INTERNAL)) {
            return props.getInt(name, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets a property of this content manager, as it is written in the
     * configuration file (helper method for implementations). Only internal
     * properties!
     *
     * @param name
     *            Name of the property to get.
     * @param defaultValue
     *            Value to return in case the given property is not set.
     * @return Value of the given property or null, if no such property exists.
     */
    public static String getProperty(String name, String defaultValue) {
        if (name.startsWith(Constants.PROPERTIES_INTERNAL)) {
            String ret = props.getString(name);
            return ((ret == null) ? defaultValue : ret);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the transfer buffer size.
     *
     * @return Transfer buffer size.
     */
    public static int getTransferBufferSize() {
        return transferBufferSize;
    }
}

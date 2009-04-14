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
package org.jboss.shotoku.jcr;

import org.apache.commons.configuration.Configuration;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.nodetype.*;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.name.QName;
import org.xml.sax.InputSource;

import javax.jcr.*;
import javax.jcr.version.OnParentVersionAction;
import javax.jcr.nodetype.NodeTypeManager;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 */
public class JackrabbitJcrConnector implements JcrConnector {
    // Declaration of constants

    // Namespaces
    private static final String SHOTOKU_JCR_NAMESPACE    = "http://labs.jboss.org/shotoku";
    private static final String SHOTOKU_JCR_NAMESPACE_SHORT    = "shotoku";
    private static final String SHOTOKU_SYS_JCR_NAMESPACE    = "http://labs.jboss.org/shotoku-system";
    private static final String SHOTOKU_SYS_JCR_NAMESPACE_SHORT    = "shotoku-system";

    // Node types
    private static final String DIR_JCR_NAME    = "directory";
    private static final String NODE_JCR_NAME   = "node";
    private static final String DIR_JCR_TYPE    = SHOTOKU_JCR_NAMESPACE_SHORT + ":" + DIR_JCR_NAME;
    private static final String NODE_JCR_TYPE   = SHOTOKU_JCR_NAMESPACE_SHORT + ":" + NODE_JCR_NAME;
    private static final String HIERARCHY_NODE  = "hierarchyNode";

    // Properties
    private static final String PROPERTY_USERNAME    = "username";
    private static final String PROPERTY_PASSWORD    = "password";
    private static final String PROPERTY_CONFIGFILE  = "configfile";
    private static final String PROPERTY_LOCALPATH   = "localpath";

    // Repository map (CM id -> Repository instance)
    private static Map<String, Repository> repositories = new HashMap<String, Repository>();

    /**
     * Gets a repository for the given id and configuration parameters.
     * This is needed to ensure that there is only one jcr-repository
     * instance for every content manager id.
     * @throws RepositoryException
     */
    private synchronized static Repository getRepository(String id, String configfile,
                                                         String localpath) throws RepositoryException {
        Repository repo = repositories.get(id);

        if (repo == null) {
            repo = RepositoryImpl.create(RepositoryConfig.create(
                new InputSource(configfile), localpath));

            repositories.put(id, repo);
        }

        return repo;
    }

    // JcrConnector implementation
    private String username;
    private char[] password;
    private Repository repository;

    public void init(Configuration conf, String id) throws Exception {
        String configfile = conf.getString(PROPERTY_CONFIGFILE);
        String localpath = conf.getString(PROPERTY_LOCALPATH);
        username = conf.getString(PROPERTY_USERNAME);
        password = conf.getString(PROPERTY_PASSWORD).toCharArray();

        // Initializing the repository
        repository = getRepository(id, configfile, localpath);

        // Checking & possibly creating the node types.
        Repository repo = getRepository();
        Session session = null;
        try {
            session = getSession(repo);
            registerNamespaces(session);
            if (!nodeTypesExist(session)) {
                registerCustomNodeTypes(session);
            }

            // Making sure that the root node is versionable.
            setNodeVersionable(session.getRootNode());
            session.save();
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public Repository getRepository() throws Exception {
        return repository;
    }

    public Session getSession(Repository repository) throws Exception {
        return repository.login(new SimpleCredentials(username, password));
    }

    public String getNodeNodeType() {
        return NODE_JCR_TYPE;
    }

    public String getDirectoryNodeType() {
        return DIR_JCR_TYPE;
    }

    public String createUserPropertyName(String prop) {
        return SHOTOKU_JCR_NAMESPACE_SHORT + ":" + prop;
    }

    public String createInternalPropertyName(String prop) {
        return SHOTOKU_SYS_JCR_NAMESPACE_SHORT + ":" + prop;
    }

    public String getDataPropertyName() {
        return "jcr:data";
    }

    public void setNodeVersionable(Node node) throws RepositoryException {
        if (!node.isNodeType("mix:versionable")) {
            node.addMixin("mix:versionable");
        }
    }

    /*
     * Helper methods.
     */

    private boolean nodeTypesExist(Session session) throws Exception {
        Workspace wosp = session.getWorkspace();
        NodeTypeManager ntMgr = wosp.getNodeTypeManager();
        NodeTypeRegistry ntReg = ((NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();
        boolean dirType = ntReg.isRegistered(new QName(SHOTOKU_JCR_NAMESPACE,
                DIR_JCR_NAME));
        boolean nodeType = ntReg.isRegistered(new QName(SHOTOKU_JCR_NAMESPACE,
                NODE_JCR_NAME));
        return dirType && nodeType;

    }

    private void registerCustomNodeTypes(Session session) throws Exception {
        registerNodeType(session, new QName(SHOTOKU_JCR_NAMESPACE,
                DIR_JCR_NAME), HIERARCHY_NODE);
        registerNodeType(session, new QName(SHOTOKU_JCR_NAMESPACE,
                NODE_JCR_NAME), HIERARCHY_NODE);
    }

    private void registerNodeType(Session session, QName qname, String superType)
    throws Exception {
        NodeTypeDef ntd = new NodeTypeDef();
        ntd.setName(qname);
        ntd.setOrderableChildNodes(false);

        PropDefImpl propDef = new PropDefImpl();
        propDef.setDeclaringNodeType(ntd.getName());
        propDef.setOnParentVersion(OnParentVersionAction.VERSION);
        ntd.setSupertypes(new QName[]{new QName("http://www.jcp.org/jcr/nt/1.0", superType)});
        ntd.setPropertyDefs(new PropDef[]{propDef});

        NodeDefImpl def = getChildNodeDef(ntd.getName(), superType);
        ntd.setChildNodeDefs(new NodeDef[]{def});

        Workspace wosp = session.getWorkspace();
        NodeTypeManager ntMgr = wosp.getNodeTypeManager();
        NodeTypeRegistry ntReg = ((NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();
        ntReg.registerNodeType(ntd);
    }

    private NodeDefImpl getChildNodeDef(QName declaringNodeType, String superType) {
        NodeDefImpl nodeDef = new NodeDefImpl();
        nodeDef.setName(new QName("", "*"));
        nodeDef.setAutoCreated(false);
        nodeDef.setMandatory(false);
        nodeDef.setProtected(false);
        nodeDef.setOnParentVersion(OnParentVersionAction.VERSION);
        nodeDef.setAllowsSameNameSiblings(false);
        nodeDef.setRequiredPrimaryTypes(new QName[] {
                new QName("http://www.jcp.org/jcr/nt/1.0", superType) });
        nodeDef.setDeclaringNodeType(declaringNodeType);
        return nodeDef;
    }

    private void registerNamespaces(Session session) throws Exception {
        Workspace wsp = session.getWorkspace();
        NamespaceRegistry registry = wsp.getNamespaceRegistry();
        /*
         * There's no way of checking if a namespace is registred, and
         * re-registering throws an exception.
         */
        try {
            registry.registerNamespace(SHOTOKU_JCR_NAMESPACE_SHORT,
                SHOTOKU_JCR_NAMESPACE);
        } catch (Exception e) { /* Doing nothing */ }
        try {
            registry.registerNamespace(SHOTOKU_SYS_JCR_NAMESPACE_SHORT,
                SHOTOKU_SYS_JCR_NAMESPACE);
        } catch (Exception e) { /* Doing nothing */ }
    }
}

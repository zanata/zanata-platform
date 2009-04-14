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

import java.util.List;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.NameFormatException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnDirectoryProxy extends SvnResourceProxy implements
        SvnDirectory {
    private SvnDirectory currentDirectory;

    public SvnDirectoryProxy(String id, String fullPath, SvnContentManager
            svnCm) {
        super(id, fullPath, svnCm);
    }

    public SvnDirectoryProxy(String id, String fullPath, SvnContentManager
            svnCm, SvnDirectory initialDirectory) {
        super(id, fullPath, svnCm, initialDirectory);

        currentDirectory = initialDirectory;
    }

    protected SvnResource getFileResource() {
        currentDirectory = new SvnFileDirectory(getId(), getFullPath(),
                getSvnCm());

        return currentDirectory;
    }

    protected SvnResource getRepoResource() {
        currentDirectory = new SvnRepoDirectory(getId(), getFullPath(),
                getSvnCm());

        return currentDirectory;
    }

    /*
      * Directory IMPLEMENTATION
      */

    public List<Directory> getDirectories() throws RepositoryException {
        checkSwitch();
        return currentDirectory.getDirectories();
    }

    public Directory getDirectory(String name) throws RepositoryException,
            ResourceDoesNotExist {
        checkSwitch();
        return currentDirectory.getDirectory(name);
    }

    public Node getNode(String name) throws RepositoryException,
            ResourceDoesNotExist {
        checkSwitch();
        return currentDirectory.getNode(name);
    }

    public NodeList getNodes() throws RepositoryException {
        checkSwitch();
        return currentDirectory.getNodes();
    }

    public boolean hasIndex(String name) throws RepositoryException {
        checkSwitch();
        return currentDirectory.hasIndex(name);
    }

    public Directory newDirectory(String name) throws ResourceAlreadyExists,
            RepositoryException, NameFormatException {
        checkSwitch();
        return currentDirectory.newDirectory(name);
    }

    public Node newNode(String name) throws ResourceAlreadyExists,
            RepositoryException, NameFormatException {
        checkSwitch();
        return currentDirectory.newNode(name);
    }

    public void setIndex(String name, boolean index) throws RepositoryException {
        checkSwitch();
        currentDirectory.setIndex(name, index);
    }
}

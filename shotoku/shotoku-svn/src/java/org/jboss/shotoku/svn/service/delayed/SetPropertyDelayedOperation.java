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
package org.jboss.shotoku.svn.service.delayed;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class SetPropertyDelayedOperation implements DelayedOperation {
    private String fullPath;
    private String propName;
    private String propValue;

    public SetPropertyDelayedOperation(String fullPath, String propName, String propValue) {
        this.fullPath = fullPath;
        this.propName = propName;
        this.propValue = propValue;
    }

    public void perform(ContentManager cm) throws RepositoryException, ResourceDoesNotExist {
        Resource res;

        try {
            res = cm.getNode(fullPath);
        } catch (ResourceDoesNotExist e) {
            res = cm.getDirectory(fullPath);
        }

        res.setProperty(propName, propValue);
        try {
            res.save("Delayed property setting: (" + propName + ", " + propValue + ")");
        } catch (SaveException e) {
            throw new RepositoryException(e);
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof SetPropertyDelayedOperation)) {
            return false;
        }

        SetPropertyDelayedOperation spdo = (SetPropertyDelayedOperation) obj;

        return Tools.objectsEqual(propName, spdo.propName) &&
                Tools.objectsEqual(propValue, spdo.propValue);
    }
}

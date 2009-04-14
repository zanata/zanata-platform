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
package org.jboss.shotoku.svn.operations;

import java.util.Map;
import java.util.Set;

import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.svn.SvnService;
import org.jboss.shotoku.svn.SvnContentManager;
import org.jboss.shotoku.common.content.NodeContent;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ModifyFileOperation extends ResourceOperation {
    private long contentLength;
    private Map<String, String> properties;
    private Set<String> deletedProperties;
    private NodeContent content;
    private SvnContentManager.ContentManagerHolder cmh;

    public ModifyFileOperation(String id, String path,
                               long contentLength, Map<String, String> properties,
                               Set<String> deletedProperties, NodeContent content,
                               SvnContentManager.ContentManagerHolder cmh) {
        super(id, path, OpCode.MODIFY_FILE);

        this.contentLength = contentLength;
        this.properties = properties;
        this.deletedProperties = deletedProperties;
        this.content = content;
        this.cmh = cmh;
    }

    public void execute(PathsStack stack, long lastRevision) throws SVNException {
        stack.accomodate(path, false, lastRevision);

        ISVNEditor editor = stack.getEditor();
        if (!stack.isFileOpened()) {
            editor.openFile(path, lastRevision);
            stack.addPath(path, true);
        }

        for (String key : properties.keySet()) {
            editor.changeFileProperty(path, key, SVNPropertyValue.create(properties.get(key)));
        }

        for (String key : deletedProperties) {
            editor.changeFileProperty(path, key, null);
        }

        if (contentLength != -1) {
            editor.applyTextDelta(path, null);

            try {
                new SVNDeltaGenerator().sendDelta(
                        path,
                        cmh.getContentManger().getNode(path).getContentInputStream(), 0, 
                        content.asInputStream(),
                        editor,
                        false);
            } catch (ResourceDoesNotExist e) {
                // This must be a new resource. Creating a delta against an empty
                // file.
                new SVNDeltaGenerator().sendDelta(
                        path,
                        content.asInputStream(),
                        editor,
                        false);
            }
        }
    }

    public void addModifiedPaths(SvnService service) {
        service.addNodeToModfied(id, path);
    }
}

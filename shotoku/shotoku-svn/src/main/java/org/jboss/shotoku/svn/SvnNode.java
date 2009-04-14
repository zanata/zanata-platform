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

import org.jboss.shotoku.Node;
import org.jboss.shotoku.common.content.NodeContent;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public interface SvnNode extends SvnResource, Node {
    /**
     * Sets this node's content to the given one. Used when switching
     * implementations (possible content changes must propagate).
     * @param content
     */
    public void setNodeContent(NodeContent content);
    /**
     * @return This node's content representation (possibly modified).
     */
    public NodeContent getNodeContent();
}

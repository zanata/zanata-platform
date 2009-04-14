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
package org.jboss.shotoku.aop;

import org.jboss.aop.Aspect;
import org.jboss.aop.Bind;
import org.jboss.aop.advice.Scope;
import org.jboss.aop.joinpoint.FieldInvocation;
import org.jboss.aop.joinpoint.FieldReadInvocation;
import org.jboss.aop.joinpoint.FieldWriteInvocation;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
@Aspect(scope=Scope.PER_JOINPOINT)
public class ResourceInjectAspect {
	@Inject
	private ContentManager cm;
	
	private Node currentNode;
	private Directory currentDirectory;
	
	private NodeInject getCurrentNodeAnnotation(FieldInvocation invocation) {
		NodeInject current = invocation.getField().getAnnotation(NodeInject.class);
		if (current == null)
			throw new RuntimeException("This aspect should be used only with " +
					"@NodeInject!");
		
		return current;
	}
	
	private DirectoryInject getCurrentDirectoryAnnotation(FieldInvocation invocation) {
		DirectoryInject current = invocation.getField().getAnnotation(DirectoryInject.class);
		if (current == null)
			throw new RuntimeException("This aspect should be used only with " +
					"@DirectoryInject!");
		
		return current;
	}
	
	@Bind(pointcut="field(org.jboss.shotoku.Directory *->@org.jboss.shotoku.aop.DirectoryInject)")
	public Object accessDirectory(FieldReadInvocation invocation) throws Throwable {
		if (currentDirectory == null) {
			currentDirectory = cm.getDirectory(getCurrentDirectoryAnnotation(invocation).value());
		}
		
		return currentDirectory;
	}
	
	@Bind(pointcut="field(org.jboss.shotoku.Node *->@org.jboss.shotoku.aop.NodeInject)")
	public Object accessNode(FieldReadInvocation invocation) throws Throwable {
		if (currentNode == null) {
			currentNode = cm.getNode(getCurrentNodeAnnotation(invocation).value());
		}
		
		return currentNode;
	}
	
	@Bind(pointcut="field(java.lang.String *->@org.jboss.shotoku.aop.NodeInject)")
	public Object accessString(FieldReadInvocation invocation) throws Throwable {
		if (currentNode == null) {
			currentNode = cm.getNode(getCurrentNodeAnnotation(invocation).value());
		}
		
		return currentNode.getContent();
	}
	
	@Bind(pointcut="field(org.jboss.shotoku.Node *->@org.jboss.shotoku.aop.NodeInject) " +
			"OR field(org.jboss.shotoku.Directory *->@org.jboss.shotoku.aop.DirectoryInject) " +
			"OR field(java.lang.String *->@org.jboss.shotoku.aop.NodeInject)")
	public Object access(FieldWriteInvocation invocation) throws Throwable {
		throw new RuntimeException("You cannot set a @XXXInject-ed variable!");
	}
}

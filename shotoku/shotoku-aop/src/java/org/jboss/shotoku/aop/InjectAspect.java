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
import org.jboss.aop.joinpoint.*;
import org.jboss.shotoku.ContentManager;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
@Aspect(scope=Scope.PER_CLASS_JOINPOINT)
public class InjectAspect {
	private static final String INJECT_POINTCUT =
		"field(org.jboss.shotoku.ContentManager *->@org.jboss.shotoku.aop.Inject)";
	
	@Bind(pointcut=INJECT_POINTCUT)
	public Object access(FieldReadInvocation invocation) throws Throwable {
		Inject current = invocation.getField().getAnnotation(Inject.class);
		if (current == null)
			throw new RuntimeException("This aspect should be used only with " +
					"@Inject!");
		
		String prefix = current.prefix();
		if (prefix == null) prefix = "";
		
		String id = current.id();

		if ((id == null) || ("".equals(id))) {
			return ContentManager.getContentManager(prefix);
		} else {
			return ContentManager.getContentManager(id, prefix);
		}
	}
	
	@Bind(pointcut=INJECT_POINTCUT)
	public Object access(FieldWriteInvocation invocation) throws Throwable {
		throw new RuntimeException("Setting an @Inject-ed variable is illegal");
	}
}

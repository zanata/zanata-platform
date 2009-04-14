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
import org.jboss.aop.joinpoint.*;
import org.jboss.aop.advice.Scope;
import org.jboss.shotoku.cache.CacheItem;
import org.jboss.shotoku.cache.CacheItemUser;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Aspect(scope=Scope.PER_JOINPOINT)
public class CacheInjectAspect {
    private final static Object synchronizer = new Object();
    private CacheItemUser<?,?> sci;

    private CacheItemInject getCurrentAnnotation(FieldInvocation invocation) {
    	CacheItemInject current = invocation.getField().getAnnotation(CacheItemInject.class);
        if (current == null)
            throw new RuntimeException("This aspect should be used only with " +
                    "@CacheItem!");

        return current;
    }

    @Bind(pointcut="field($instanceof{org.jboss.shotoku.cache.CacheItemUser} " +
            "*->@org.jboss.shotoku.aop.CacheItemInject)")
    public Object accessCacheItem(FieldReadInvocation invocation) throws Throwable {
        if (sci == null) {
            synchronized(synchronizer) {
                if (sci == null) {
                	CacheItemInject ci = getCurrentAnnotation(invocation);

                    sci = CacheItem.create(ci.dataSource().newInstance(), null, null, ci.interval());
                }
            }
        }

        return sci;
    }

    @Bind(pointcut="field(org.jboss.shotoku.cache.CacheItemUser " +
            "*->@org.jboss.shotoku.aop.CacheItemInject)")
    public Object access(FieldWriteInvocation invocation) throws Throwable {
        throw new RuntimeException("You cannot set a @CacheItem or a " +
                "@ResourceWatcher variable!");
    }
}

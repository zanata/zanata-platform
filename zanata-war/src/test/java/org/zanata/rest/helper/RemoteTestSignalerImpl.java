/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.seam2.ReflectionHelper;
import javax.inject.Named;
import org.zanata.arquillian.RemoteAfter;
import org.zanata.arquillian.RemoteBefore;

/**
 * Default implementation for the Remote Signaler interface.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @see RemoteTestSignaler
 */
@Path("/test/remote/signal")
@Named("remoteTestSignalerImpl")
@Slf4j
public class RemoteTestSignalerImpl implements RemoteTestSignaler {
    @Override
    public void signalBeforeTest(String testClass, String testMethod) throws Exception {
        log.info("Starting test {}:{}", testClass, testMethod);
        Class<?> testCls = Class.forName(testClass);
        Object testInstance = testCls.newInstance();

        invokeAnnotatedMethods(testInstance, RemoteBefore.class);
    }

    @Override
    public void signalAfterTest(String testClass, String testMethod) throws Exception {
        Class<?> testCls = Class.forName(testClass);
        Object testInstance = testCls.newInstance();

        invokeAnnotatedMethods(testInstance, RemoteAfter.class);
        log.info("Finished test {}:{}", testClass, testMethod);
    }

    private void invokeAnnotatedMethods(Object o,
            Class<? extends Annotation> annotation)
            throws InvocationTargetException, IllegalAccessException {
        List<Method> beforeMethods = new ArrayList<Method>();
        Class<?> objClass = o.getClass();
        while (objClass != null) {
            beforeMethods.addAll(ReflectionHelper.getMethodsWithAnnotation(
                    objClass, annotation));
            objClass = objClass.getSuperclass();
        }

        for (Method m : beforeMethods) {
            m.invoke(o);
        }
    }

}

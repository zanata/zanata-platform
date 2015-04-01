/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.zanata.util;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.testng.annotations.Test;
import org.zanata.events.ServerStarted;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.testng.Assert.fail;

/**
 * TODO: remove this class after moving to CDI
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Test
public class EventPayloadTest {
    public void payloadClassShouldHaveCorrectEVENT_NAME() throws IllegalAccessException {
        Reflections reflections = new Reflections(ServerStarted.class.getPackage().getName()+".", new SubTypesScanner(false));
        Set<Class<? extends Object>> payloadClasses =
                reflections.getSubTypesOf(Object.class);

        for (Class clazz : payloadClasses) {
            if (Modifier.isAbstract(clazz.getModifiers()) ||
                    clazz.isAnnotation() ||
                    clazz.getName().endsWith("Test")) {
                continue;
            }
//            System.out.println(clazz);
            Field nameField = null;
            try {
                nameField = clazz.getField("EVENT_NAME");
            } catch (NoSuchFieldException e) {
                fail("Can't find EVENT_NAME in " + clazz);
            }
            String eventName = (String) nameField.get(null);
            if (!eventName.equals(clazz.getName())) {
                fail("EVENTNAME " + eventName + " does not match class name " + clazz.getName());
            }
        }
        if (payloadClasses.isEmpty()) {
            fail("Could not find any event payload classes");
        }
    }
}

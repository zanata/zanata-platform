/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import com.google.common.collect.ImmutableMap;
import org.hibernate.annotations.common.annotationfactory.AnnotationDescriptor;
import org.hibernate.annotations.common.annotationfactory.AnnotationFactory;
import org.zanata.security.annotations.Authenticated;
import org.zanata.servlet.annotations.HttpParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Creates dummy implementations of Annotation interfaces.
 * <p>
 * Warning: if an annotation has any members without default values, you must
 * provide a Map containing those values, or an exception will occur when those
 * values are requested.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class Annotations {

    public static Annotation[] dummy(Class<? extends Annotation>[] qualifiers) {
        return Arrays.stream(qualifiers).map(Annotations::dummy).toArray(
                Annotation[]::new);
    }

    public static <A extends Annotation> A dummy(Class<A> clazz) {
        return create(clazz, ImmutableMap.of());
    }

    public static <A extends Annotation> A create(Class<? extends Annotation> clazz,
            Map<String, Object> attributes) {
        AnnotationDescriptor desc = new AnnotationDescriptor(clazz);
        attributes.forEach(desc::setValue);
        return AnnotationFactory.create(desc);
    }

    public static void main(String[] args)
            throws Exception {
        Authenticated auth = dummy(Authenticated.class);
        System.out.println(auth);
        HttpParam httpParam =
                create(HttpParam.class, ImmutableMap.of("value", "param"));
        System.out.println(httpParam);
        Method[] methods =
                HttpParam.class.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(
                    method.getName() + "=" + method.invoke(httpParam));
        }
    }

}

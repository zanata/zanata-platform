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

package org.zanata.rest.service;

import java.util.Set;
import javax.ws.rs.core.Application;

import com.google.common.collect.ImmutableSet;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockResourcesApplication extends Application {

    private static final ImmutableSet<Class<?>> services;

    static {
        ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
        builder.add(
                MockVersionResource.class,
                MockSourceDocResource.class,
                MockTranslatedDocResource.class,
                MockStatisticsResource.class,
                MockFileResource.class,
                MockProjectsResource.class,
                MockProjectResource.class,
                MockGlossaryResource.class,
                MockCopyTransResource.class,
                MockAccountResource.class,
                MockAsynchronousProcessResource.class
        );
        addDeprecatedResource(builder);
        services = builder.build();
    }

    @SuppressWarnings("deprecation")
    //TODO: replace with MockProjectVersionResource
    private static void addDeprecatedResource(
            ImmutableSet.Builder<Class<?>> builder) {
        builder.add(MockProjectIterationResource.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return services;
    }
}


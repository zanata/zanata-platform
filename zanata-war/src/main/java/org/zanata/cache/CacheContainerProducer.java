/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.cache;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.manager.CacheContainer;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.enterprise.inject.Produces;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Zanata;

import javax.naming.NamingException;

/**
 * Produces a cache container for injection.
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("cacheContainer")
@ApplicationScoped
@Slf4j
public class CacheContainerProducer {

    private static final String CACHE_CONTAINER_NAME =
            "java:jboss/infinispan/container/zanata";

    @Resource(lookup = CACHE_CONTAINER_NAME)
    private CacheContainer container;

    @Produces
    @ApplicationScoped
    @Zanata
    public CacheContainer getCacheContainer() {
        return container;
    }
}

// Implementation copied from Seam 2.3.1, commit f3077fe

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.zanata.seam.resteasy;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * TODO: We need to significantly extend and change that class so we can lookup
 * provider instances through Seam at runtime. The original class has only been
 * designed for registration of "singleton" providers during startup. See
 * comment about the TL handling in ResteasyResourceAdapter.java.
 *
 * @author Christian Bauer
 */
public class SeamResteasyProviderFactory extends ResteasyProviderFactory {

    public static void setInstance(ResteasyProviderFactory factory) {
        ResteasyProviderFactory.setInstance(factory);
    }

    public static ResteasyProviderFactory getInstance() {
        // workaround for https://issues.jboss.org/browse/RESTEASY-1119
        // can this cause a memory leak?
        // TODO remove when https://issues.jboss.org/browse/RESTEASY-1119
        // fix is released
        ResteasyProviderFactory factory =
                ResteasyProviderFactory.getInstance();
        ResteasyProviderFactory.pushContext(javax.ws.rs.ext.Providers.class,
                factory);
        return factory;
    }

}

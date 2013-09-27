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
package org.zanata.arquillian;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.seam2.configuration.ConfigurationImporter;
import org.jboss.arquillian.seam2.configuration.Seam2Configuration;
import org.jboss.arquillian.seam2.configuration.Seam2ConfigurationProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 *
 * Triggers configuration creation on the client side.
 *
 * This is an extended listener on the Arquillian Suite Extension to make it
 * work with Arquillian Seam2 extension 1.0.0.Alpha2. Once 1.0.0.Final is
 * released, this class should not be needed and it should stop compiling.
 *
 * @author <a href="mailto:camunoz@redhat.com">Carlos Munoz</a>
 *
 */
public class Seam2ExtendedConfigurationProducer {
    @Inject
    @ApplicationScoped
    InstanceProducer<Seam2Configuration> configurationProducer;

    public void configure(@Observes ArquillianDescriptor descriptor) {
        ConfigurationImporter extractor = new ConfigurationImporter();
        Seam2Configuration configuration = extractor.from(descriptor);
        configurationProducer.set(configuration);
    }

    // DO NOT USE!
    private static final void dummyConfigure() {
        new Seam2ConfigurationProducer().configure((BeforeSuite) null);
    }

}

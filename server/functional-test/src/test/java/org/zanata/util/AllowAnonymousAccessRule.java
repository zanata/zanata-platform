/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import org.junit.rules.ExternalResource;
import com.google.common.base.Throwables;

import static org.zanata.util.SampleDataResourceClient.allowAnonymousUser;

/**
 * This will allow anonymous user to access Zanata read only resources.
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class AllowAnonymousAccessRule extends ExternalResource {


    @Override
    protected void before() throws Throwable {
        allowAnonymousUser(true);
    }

    @Override
    protected void after() {
        try {
            allowAnonymousUser(false);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}

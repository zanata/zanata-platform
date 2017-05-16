/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.service.impl;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;

import javax.inject.Named;

import com.google.common.util.concurrent.Striped;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@SuppressFBWarnings(value = { "GBU_GUAVA_BETA_CLASS_USAGE",
        "SE_BAD_FIELD" }, justification = "field Striped<Lock>")
@Named("activityLockManager")
@javax.enterprise.context.ApplicationScoped
public class ActivityLockManager implements Serializable {
    private static final int NUM_STRIPES = Runtime.getRuntime().availableProcessors() * 4;
    public ActivityLockManager() {
    }

    private Striped<Lock> stripedLock = Striped.lock(NUM_STRIPES);

    public Lock getLock(Long personId) {
        return stripedLock.get(personId);
    }


}

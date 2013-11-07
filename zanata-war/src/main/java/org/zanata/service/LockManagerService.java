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
package org.zanata.service;

import org.zanata.lock.Lock;
import org.zanata.lock.LockNotAcquiredException;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface LockManagerService {
    /**
     * Checks for the availability of a lock and attains it if available.
     *
     * @param l
     *            The lock to attain.
     * @return True, if the lock was attained. False if the lock was not
     *         available.
     */
    public boolean checkAndAttain(Lock l);

    /**
     * Checks for the availability of a lock and attains it if available.
     *
     * @param lock
     *            The lock to attain.
     * @return null if the lock was obtained, otherwise the username of the lock
     *         owner
     */
    public String attainLockOrReturnOwner(Lock lock);

    /**
     * Attains a lock.
     *
     * @param l
     *            The lock to attain.
     * @throws LockNotAcquiredException
     *             If the lock was not acquired.
     */
    public void attain(Lock l) throws LockNotAcquiredException;

    /**
     * Releases a lock.
     *
     * @param l
     *            The lock to release.
     */
    public void release(Lock l);
}

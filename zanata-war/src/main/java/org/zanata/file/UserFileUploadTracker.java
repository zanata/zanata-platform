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
package org.zanata.file;

import javax.inject.Named;

import java.util.HashSet;
import java.util.Set;

/**
 * Service to track which users are currently uploading files.
 *
 * This is intended to limit each user to a single upload operation at a time
 * to prevent an individual user tying up too many server resources.
 */
@Named("userFileUploadTracker")
@javax.enterprise.context.ApplicationScoped

public class UserFileUploadTracker {

    Set<Long> uploadingUsers = new HashSet<Long>();

    /**
     * Attempt to register a user as uploading a file.
     *
     * The attempt will succeed if the user is not already uploading a file,
     * and will fail otherwise.
     *
     * The check and registration are performed in a single operation to reduce the
     * chance of race conditions.
     *
     * @param userId for which to register an upload.
     * @return true if this attempt to register the user as uploading is successful.
     */
    public boolean tryToRegisterUserForFileUpload(Long userId) {
        if (uploadingUsers.contains(userId)) {
            return false;
        }
        uploadingUsers.add(userId);
        return true;
    }

    public boolean isUserUploading(Long userId) {
        return uploadingUsers.contains(userId);
    }

    /**
     * Reverse a registration for uploading a file, as registered using
     * {@link #tryToRegisterUserForFileUpload(Long)}.
     *
     * The given userId will not be registered for upload when this call completes.
     *
     * @param userId to de-register
     */
    public void deRegisterUserForFileUpload(Long userId) {
        uploadingUsers.remove(userId);
    }

}

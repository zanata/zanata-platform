/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model;

/**
 * Will give a user friendly string representation of the object. All targets in
 * SecurityFunctions should implement this interface so that when the permission
 * check fail and we throw an AuthorizationException, the message will be more
 * meaningful.
 *
 * @see org.jboss.seam.security.SecurityFunctions
 * @see org.zanata.security.ZanataIdentity#checkPermission(java.lang.String,
 *      java.lang.Object...)
 */
public interface HasUserFriendlyToString {

    String userFriendlyToString();
}

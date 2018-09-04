/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.util

/**
 * When using mvnw, the Maven process has maven.home pointing at the maven install.
 * Failsafe is configured to propagate maven.home as a system property.
 *
 * This tries to return the full path to the script "mvn" in the Maven home
 * directory (if maven.home is set), otherwise returns simply "mvn" for
 * resolution via system PATH.
 *
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */

private val mavenHome = System.getProperty("maven.home")
val mvn = if (mavenHome != null) "$mavenHome/bin/mvn" else "mvn"


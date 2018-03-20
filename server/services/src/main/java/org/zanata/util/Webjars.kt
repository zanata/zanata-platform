/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

import javax.enterprise.context.ApplicationScoped
import javax.inject.Named
import java.io.Serializable

/**
 * @author Sean Flanigan
 * [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@Named("webjars")
@ApplicationScoped
class Webjars : Serializable {

    companion object {
        private const val serialVersionUID = 1L

        private val DIFF_VER: String = Dependencies.getVersion("org.webjars.npm:diff:jar")
        private val DIFF_SCRIPT =
                "diff/$DIFF_VER/dist/diff.min.js"
    }

    /**
     * Return the name of the 'diff' script for JSF h:outputScript.
     *
     * You can use it like this:
     *
     * <pre>
     * `<h:outputScript target="body" library="webjars"
     * name="${webjars.diff}"/>`
     * </pre>
     *
     * @return
     */
    val diff: String = DIFF_SCRIPT

}

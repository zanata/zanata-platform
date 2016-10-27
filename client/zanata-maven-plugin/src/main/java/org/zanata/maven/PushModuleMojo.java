/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

package org.zanata.maven;

/**
 * Pushes source text to a Zanata project version so that it can be translated,
 * and optionally push translated text as well. NB: Any documents which exist on
 * the server but not locally will be deleted as obsolete. If
 * deleteObsoleteModules is true, documents belonging to unknown/obsolete
 * modules will be deleted as well.
 *
 * @goal push-module
 * @requiresProject true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PushModuleMojo extends AbstractPushMojo {
    /**
     * Remove modules that are found in the server but not locally.
     *
     * @parameter expression="${zanata.deleteObsoleteModules}"
     *            default-value="false"
     */
    private boolean deleteObsoleteModules;

    @Override
    public boolean getEnableModules() {
        return true;
    }

    @Override
    public boolean getDeleteObsoleteModules() {
        return this.deleteObsoleteModules;
    }
}

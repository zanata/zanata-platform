/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands;

import com.google.common.collect.ImmutableSet;

import java.io.File;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public interface PushPullOptions extends ConfigurableProjectOptions {

    /**
     * Character or characters which must appear after moduleIDs when
     * constructing qualified document names
     *
     * @return
     */
    String getModuleSuffix();

    /**
     * A regex for document names, which captures the module ID as group 1 and
     * the unqualified docname as group 2. This regex should broadly follow the
     * pattern "(moduleID)moduleSuffix(localDocName)".
     */
    String getDocNameRegex();

    boolean isDryRun();

    File getSrcDir();

    String getSrcDirParameterName();

    File getTransDir();

    String getFromDoc();

    /**
     * This name should represent the exact parameter as it would be entered on
     * the command line, and include any space or operators that would link the
     * parameter to the argument. This is so that the argument can be appended
     * directly to the parameter name.
     */
    String buildFromDocArgument(String argValue);

    boolean getEnableModules();

    boolean isRootModule();

    String getCurrentModule();

    /**
     * @param canonicalName
     *            true if the module name should be in the canonical format used
     *            by maven
     * @return
     */
    String getCurrentModule(boolean canonicalName);

    ImmutableSet<String> getAllModules();
}

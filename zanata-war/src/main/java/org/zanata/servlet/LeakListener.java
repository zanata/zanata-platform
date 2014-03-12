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

package org.zanata.servlet;

import java.util.logging.Level;

import lombok.extern.java.Log;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventor;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Log
public class LeakListener extends ClassLoaderLeakPreventor {
    protected void debug(String s) {
        log.fine(s);
    }

    protected void info(String s) {
        log.info(s);
    }

    protected void warn(String s) {
        log.warning(s);
    }

    protected void warn(Throwable t) {
        log.log(Level.WARNING, "", t);
    }

    protected void error(String s) {
        log.severe(s);
    }

    protected void error(Throwable t) {
        log.log(Level.SEVERE, "", t);
    }

}

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

import java.util.Collection;

import org.zanata.process.ProcessHandle;
import org.zanata.process.RunnableProcess;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface ProcessManagerService
{
   /**
    * Starts a process in the background and returns immediately. The process is also indexed by the
    * given keys (if any). To retrieve a process handle, any of the provided keys can be used.
    *
    * Keys should override the equals and hashCode methods.
    *
    * @param process The process to run in the background.
    * @param handle The unused process handle to communicate with the running process.
    * @param keys Keys to index the process with.
    * @param <H>
    */
   public <H extends ProcessHandle> void startProcess( RunnableProcess<H> process, H handle, Object ... keys );

   /**
    * Returns a process handle using its unique, automatically generated id.
    *
    * @param processId The process id to find.
    * @return A Process handle if one is found. Null otherwise.
    */
   public ProcessHandle getProcessHandle( String processId );

   /**
    * Returns a process handle using one of the originally used keys.
    *
    * @param key The key to look for a process.
    * @return A Process handle if one is found. Null otherwise.
    */
   public ProcessHandle getProcessHandle( Object key );

   public Collection<ProcessHandle> getAllActiveProcessHandles();

   public Collection<ProcessHandle> getAllInactiveProcessHandles();

   public void clearInactive();
}

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
package org.zanata.service.impl;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.zanata.lock.Lock;
import org.zanata.service.LockManagerService;

/**
 * Default implementation of the {@link LockManagerService} interface.
 * Manages locks at the application level.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("lockManagerServiceImpl")
@Scope(ScopeType.APPLICATION)
@Startup
public class LockManagerServiceImpl implements LockManagerService
{
   private final Set<Lock> locks = Collections.newSetFromMap(new ConcurrentHashMap<Lock, Boolean>());

   @Override
   public synchronized boolean attain(Lock l)
   {
      if( locks.contains( l ) )
      {
         return false;
      }
      else
      {
         locks.add(l);
         return true;
      }
   }

   @Override
   public void release(Lock l)
   {
      locks.remove(l);
   }
}

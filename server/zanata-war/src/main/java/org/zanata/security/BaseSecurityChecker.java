/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.security;

import org.jboss.seam.annotations.In;
import org.jboss.seam.security.Identity;

/**
 * Base class for beans that want to implement basic permission check functionality.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public abstract class BaseSecurityChecker implements SecurityChecker
{

   @In
   protected Identity identity;
   
   /**
    * Returns the object that is to be checked for permissions.
    */
   public abstract Object getSecuredEntity();
   
   /**
    * Checks for permissions to the indicated operation on the object returned by the
    * <code>getSecuredEntity</code> method.
    * 
    * @see SecurityChecker#checkPermission(String)
    */
   @Override
   public boolean checkPermission(String operation)
   {
      return identity != null && identity.hasPermission(getSecuredEntity(), operation);
   }
}

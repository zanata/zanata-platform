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
package org.zanata.seam.scope;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Hand-written implementation of a pseudo-flash scope. The variables in this component will be
 * available until cleared from the conversation, or the conversation itself ends.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 *
 * @deprecated This should be replaced by JSF2's flash scope implementation.
 */
@Name("flashScope")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
@Deprecated
public class FlashScopeBean implements Serializable
{
   private Map<String, Object> attributes = new HashMap<String, Object>();

   @Begin(join = true)
   public void setAttribute(String name, Object value)
   {
      attributes.put(name, value);
   }

   public Object getAttribute( String name )
   {
      return attributes.get(name);
   }

   public Object getAndClearAttribute( String name )
   {
      return attributes.remove( name );
   }

   public boolean hasAttribute( String name )
   {
      return attributes.containsKey(name);
   }
}

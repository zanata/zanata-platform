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
package org.zanata.config;

import java.util.Collection;

/**
 * Configuration store interface. Configuration stores are a collection of key-indexed values.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface ConfigurationStore<K, V>
{
   /**
    * Returns a configuration value based on a key.
    *
    * @param key The key
    * @return The value stored for key. May return null if there is no such key.
    */
   V getConfigValue(K key);

   /**
    * Indicates if a key is contained in the store.
    *
    * @param key The key
    * @return True, if the key is contained in the store (no guarantees on the value). False otherwise.
    */
   boolean containsKey(K key);

   /**
    * Resets the store by clearing out all values.
    */
   void reset();
}

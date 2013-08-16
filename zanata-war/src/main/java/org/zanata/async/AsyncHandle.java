/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.async;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.AbstractFuture;

import lombok.Getter;
import lombok.Setter;

/**
 * Asynchronous handle to provide communication between an asynchronous process
 * and interested clients.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncHandle<V> extends AbstractFuture<V>
{
   @Getter @Setter
   public int maxProgress;

   @Getter @Setter
   public int minProgress;

   @Getter @Setter
   public int currentProgress;

   @Override
   protected boolean setException(Throwable throwable)
   {
      return super.setException(throwable);
   }

   @Override
   protected boolean set(@Nullable V value)
   {
      return super.set(value);
   }
}

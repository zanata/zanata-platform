/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.model;

import java.util.Arrays;
import java.util.List;

import lombok.Data;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;

import com.google.common.collect.ImmutableList;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Data
public class SimpleTargetContents implements TargetContents
{
   private final ContentState state;
   private List<String> contents;
   private final LocaleId localeId;

   public SimpleTargetContents(LocaleId localeId, ContentState state, List<String> contents)
   {
      this.localeId = localeId;
      this.state = state;
      this.contents = contents;
   }

   public SimpleTargetContents(LocaleId localeId, ContentState state, String... contents)
   {
      this(localeId, state, ImmutableList.copyOf(contents));
   }

   // Lombok won't generate this because of the other setContents method
   @Override
   public void setContents(List<String> contents)
   {
      this.contents = contents;
   }

   @Override
   public void setContents(String... contents)
   {
      setContents(Arrays.asList(contents));
   }

}

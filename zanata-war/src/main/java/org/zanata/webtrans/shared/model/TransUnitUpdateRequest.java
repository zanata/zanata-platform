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

package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.List;

import org.zanata.common.ContentState;
import com.google.common.base.Objects;

/**
 * Represents a request to update the translation for a {@link TransUnit},
 * encapsulating the details of the update
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class TransUnitUpdateRequest implements Serializable
{

   private static final long serialVersionUID = 1L;

   private TransUnitId transUnitId;
   private List<String> newContents;
   private ContentState newContentState;
   private int baseTranslationVersion;

   // required for GWT rpc serialization
   @SuppressWarnings("unused")
   private TransUnitUpdateRequest()
   {
   }

   public TransUnitUpdateRequest(TransUnitId transUnitId, List<String> newContents, ContentState newContentState, int baseTranslationVersion)
   {
      this.transUnitId = transUnitId;
      this.newContents = newContents;
      this.newContentState = newContentState;
      this.baseTranslationVersion = baseTranslationVersion;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public List<String> getNewContents()
   {
      return newContents;
   }

   public ContentState getNewContentState()
   {
      return newContentState;
   }

   public int getBaseTranslationVersion()
   {
      return baseTranslationVersion;
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("transUnitId", transUnitId).
            add("newContents", newContents).
            add("newContentState", newContentState).
            add("baseTranslationVersion", baseTranslationVersion).
            toString();
   }
}

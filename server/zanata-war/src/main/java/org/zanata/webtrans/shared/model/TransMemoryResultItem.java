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
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TransMemoryResultItem extends SearchResultItem implements Serializable, IsSerializable
{
   private static final long serialVersionUID = 1L;

   private ArrayList<String> sourceContents;
   private ArrayList<String> targetContents;

   // for GWT
   @SuppressWarnings("unused")
   private TransMemoryResultItem()
   {
   }

   /**
    * @param sourceContents
    * @param targetContents
    * @param relevanceScore
    * @param similarityPercent
    */
   public TransMemoryResultItem(ArrayList<String> sourceContents, ArrayList<String> targetContents, double relevanceScore, double similarityPercent)
   {
      super(relevanceScore, similarityPercent);
      this.sourceContents = sourceContents;
      this.targetContents = targetContents;
   }

   // FIXME remove this
   @Deprecated
   public String getSource()
   {
      if (sourceContents.size() == 0)
      {
         return null;
      }
      return sourceContents.get(0);
   }

   public ArrayList<String> getSourceContents()
   {
      return sourceContents;
   }

   // FIXME remove this
   @Deprecated
   public String getTarget()
   {
      if (targetContents.size() == 0)
      {
         return null;
      }
      return targetContents.get(0);
   }

   public ArrayList<String> getTargetContents()
   {
      return targetContents;
   }

}

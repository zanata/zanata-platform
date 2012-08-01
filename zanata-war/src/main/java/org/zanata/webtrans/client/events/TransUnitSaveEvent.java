/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.events;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitSaveEvent extends GwtEvent<TransUnitSaveEventHandler>
{
   public static Type<TransUnitSaveEventHandler> TYPE = new Type<TransUnitSaveEventHandler>();
   public static final TransUnitSaveEvent CANCEL_EDIT_EVENT = new TransUnitSaveEvent(null, null, null, null);

   private TransUnitId transUnitId;
   private Integer verNum;
   private List<String> targets = Lists.newArrayList();
   private ContentState status;

   public TransUnitSaveEvent(List<String> targets, ContentState status, TransUnitId transUnitId, Integer verNum)
   {
      this.targets = targets;
      this.status = status;
      this.transUnitId = transUnitId;
      this.verNum = verNum;
   }

   public Type<TransUnitSaveEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(TransUnitSaveEventHandler handler)
   {
      handler.onTransUnitSave(this);
   }

   public List<String> getTargets()
   {
      return targets;
   }

   public ContentState getStatus()
   {
      return status;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public Integer getVerNum()
   {
      return verNum;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      TransUnitSaveEvent that = (TransUnitSaveEvent) o;
      // @formatter:off
      return Objects.equal(transUnitId, that.transUnitId)
            && Objects.equal(verNum, that.verNum)
            && Objects.equal(status, that.status)
            && Objects.equal(targets, that.targets);
      // @formatter:on
   }

   @Override
   public int hashCode()
   {
      return Objects.hashCode(transUnitId, verNum, targets, status);
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("transUnitId", transUnitId).
            add("verNum", verNum).
            toString();
   }
}

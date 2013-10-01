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
package org.zanata.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Access(AccessType.FIELD)
@EqualsAndHashCode(exclude = "versionNum")
@Getter
@Setter
public class ModelEntityBase implements Serializable, HashableState
{
   private static final long serialVersionUID = -6139220551322868743L;

   @Id
   @GeneratedValue
   @Setter(AccessLevel.PROTECTED)
   protected Long id;

   @Temporal(TemporalType.TIMESTAMP)
   @Column(nullable = false)
   protected Date creationDate;

   @Temporal(TemporalType.TIMESTAMP)
   @Column(nullable = false)
   protected Date lastChanged;

   @Version
   @Column(nullable = false)
   protected Integer versionNum;

   @SuppressWarnings("unused")
   @PrePersist
   private void onPersist()
   {
      Date now = new Date();
      if (creationDate == null)
      {
         creationDate = now;
      }
      if (lastChanged == null)
      {
         lastChanged = now;
      }
   }

   @SuppressWarnings("unused")
   @PostPersist
   private void postPersist()
   {
      if (logPersistence())
      {
         Logger log = LoggerFactory.getLogger(getClass());
         log.info("persist entity: {}", this);
      }
   }

   @SuppressWarnings("unused")
   @PreUpdate
   private void onUpdate()
   {
      lastChanged = new Date();
   }

   @SuppressWarnings("unused")
   @PreRemove
   private void onRemove()
   {
      if (logPersistence())
      {
         Logger log = LoggerFactory.getLogger(getClass());
         log.info("remove entity: {}", this);
      }
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "[id=" + id + ",versionNum=" + versionNum + "]";
   }

   protected boolean logPersistence()
   {
      return true;
   }

   @Override
   public void writeHashState(ByteArrayOutputStream buff) throws IOException
   {
      buff.write(versionNum.byteValue());
   }
}

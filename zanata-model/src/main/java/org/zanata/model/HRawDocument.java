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
package org.zanata.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.DocumentType;

import com.google.common.base.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor // is this necessary?
@Access(AccessType.FIELD)
public class HRawDocument extends ModelEntityBase implements Serializable
{

   private static final long serialVersionUID = 5129552589912687504L;

   // TODO ensure any document deletion cascades to remove associated HRawDocument
   @OneToOne(mappedBy = "rawDocument")
   private HDocument document;

   // TODO none of these should allow null
   @NotEmpty
   private String contentHash;

   private String fileId;

   @Enumerated(EnumType.STRING)
   private DocumentType type;

   private String uploadedBy;

   private String adapterParameters;

   public void setDocument(HDocument document)
   {
      if (!Objects.equal(this.document, document))
      {
         this.document = document;
      }
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
            + "[id=" + id + ",versionNum=" + versionNum
            + ",contentHash=" + contentHash + "]";
   }

   // TODO override equals to use contentHash, type, parameters, etc.

}

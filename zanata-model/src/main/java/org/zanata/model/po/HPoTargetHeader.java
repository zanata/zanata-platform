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
package org.zanata.model.po;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author sflaniga@redhat.com
 * @see org.zanata.rest.dto.extensions.gettext.PoTargetHeader
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Setter
@Getter
@Access(AccessType.FIELD)
@ToString(callSuper = true, of = "targetLanguage")
public class HPoTargetHeader extends PoHeaderBase
{
   private static final long serialVersionUID = 1L;

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "targetLanguage", nullable = false)
   private HLocale targetLanguage;

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "document_id")
   private HDocument document;
}

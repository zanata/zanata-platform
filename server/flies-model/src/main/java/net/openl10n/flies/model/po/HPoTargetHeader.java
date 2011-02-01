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
package net.openl10n.flies.model.po;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HLocale;

import org.hibernate.annotations.NaturalId;

/**
 * 
 * @author sflaniga@redhat.com
 * @see net.openl10n.flies.rest.dto.po.PoTargetHeader
 * @see net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader
 */
@Entity
public class HPoTargetHeader extends AbstractPoHeader
{

   private static final long serialVersionUID = 1L;

   private HLocale targetLanguage;
   private HDocument document;

   public void setTargetLanguage(HLocale targetLanguage)
   {
      this.targetLanguage = targetLanguage;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "targetLanguage", nullable = false)
   public HLocale getTargetLanguage()
   {
      return targetLanguage;
   }

   public void setDocument(HDocument document)
   {
      this.document = document;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "document_id")
   public HDocument getDocument()
   {
      return document;
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HPoTargetHeader(" + super.toString() + "lang:" + getTargetLanguage() + ")";
   }

}

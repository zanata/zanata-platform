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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Setter
@ToString
@NoArgsConstructor
public class HEditorOption extends ModelEntityBase
{
   public static enum OptionName
   {
      EnterSavesApproved("editor.EnterSavesApproved"),
      DisplayButtons("editor.DisplayButtons"),
      PageSize("editor.PageSize"),
      ShowErrors("editor.ShowErrors"),
      TranslatedMessageFilter("editor.TranslatedMessageFilter"),
      NeedReviewMessageFilter("editor.NeedReviewMessageFilter"),
      UntranslatedMessageFilter("editor.UntranslatedMessageFilter"),
      Navigation("editor.Navigation");

      private String persistentName;

      OptionName(String persistentName)
      {
         this.persistentName = persistentName;
      }

      public String getPersistentName()
      {
         return persistentName;
      }
   }

   private String name;

   private String value;

   private HAccount account;


   public HEditorOption(OptionName name, String value)
   {
      this.name = name.getPersistentName();
      this.value = value;
   }

   public String getName()
   {
      return name;
   }

   public String getValue()
   {
      return value;
   }

   @ManyToOne(optional = false)
   @JoinColumn(name = "account_id")
   public HAccount getAccount()
   {
      return account;
   }

   @Transient
   public Boolean getValueAsBoolean()
   {
      return Boolean.parseBoolean(getValue());
   }

   @Transient
   public Integer getValueAsInt()
   {
      return Integer.parseInt(getValue());
   }
}

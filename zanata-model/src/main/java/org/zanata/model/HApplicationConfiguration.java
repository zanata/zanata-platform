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

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HApplicationConfiguration extends ModelEntityBase
{

   public static String KEY_HOST             = "flies.host";
   public static String KEY_REGISTER         = "flies.register";
   public static String KEY_DOMAIN           = "flies.email.domain";
   public static String KEY_ADMIN_EMAIL      = "email.admin.addr";
   public static String KEY_EMAIL_FROM_ADDRESS = "email.from.addr";
   public static String KEY_HOME_CONTENT     = "flies.home.content";
   public static String KEY_HELP_CONTENT     = "flies.help.content";
   public static String KEY_LOGINCONFIG_URL  = "zanata.login-config.url";
   private static final long serialVersionUID = 8652817113098817448L;

   private String key;
   private String value;

   // TODO PERF @NaturalId(mutable=false) for better criteria caching
   @NaturalId
   @NotEmpty
   @Length(max = 255)
   @Column(name = "config_key", nullable = false)
   public String getKey()
   {
      return key;
   }

   @NotNull
   @Type(type = "text")
   @Column(name = "config_value", nullable = false)
   public String getValue()
   {
      return value;
   }
}

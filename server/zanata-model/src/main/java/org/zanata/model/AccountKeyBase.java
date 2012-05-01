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

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

@MappedSuperclass
public class AccountKeyBase
{

   private String keyHash;
   private HAccount account;

   @NotEmpty
   @Length(min = 32, max = 32)
   @Id
   public String getKeyHash()
   {
      return keyHash;
   }

   public void setKeyHash(String keyHash)
   {
      this.keyHash = keyHash;
   }

   @OneToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "accountId")
   public HAccount getAccount()
   {
      return account;
   }

   public void setAccount(HAccount account)
   {
      this.account = account;
   }

}

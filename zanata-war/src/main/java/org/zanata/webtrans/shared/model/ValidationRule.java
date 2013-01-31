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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class ValidationRule implements IsSerializable
{
   private ValidationId id;
   private String description;
   private boolean enabled;
   private ArrayList<ValidationRule> exclusiveValidations = new ArrayList<ValidationRule>();

   private ValidationRule()
   {
   }

   public ValidationRule(ValidationId id, String description, boolean enabled)
   {
      this.id = id;
      this.description = description;
      this.enabled = enabled;
   }

   public ValidationId getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }

   public boolean isEnabled()
   {
      return enabled;
   }

   public List<ValidationRule> getExclusiveValidations()
   {
      return exclusiveValidations;
   }

   public void mutuallyExclusive(ValidationRule... exclusiveValidations)
   {
      this.exclusiveValidations = Lists.newArrayList(exclusiveValidations);
   }

}

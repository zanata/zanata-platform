/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.shared.validation.action;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.shared.validation.ValidationObject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public abstract class ValidationAction implements ValidationObject
{
   private String id;
   private boolean isEnabled;
   
   private final String description;

   private List<String> errorList = new ArrayList<String>();

   public abstract void validate(String source, String target);

   public ValidationAction(String id, String description)
   {
      this.id = id;
      this.description = description;
   }

   @Override
   public boolean isEnabled()
   {
      return isEnabled;
   }

   public void setEnabled(boolean isEnabled)
   {
      this.isEnabled = isEnabled;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getDescription()
   {
      return description;
   }

   @Override
   public boolean hasError()
   {
      return !errorList.isEmpty();
   }

   @Override
   public List<String> getError()
   {
      return errorList;
   }

   public void clearErrorMessage()
   {
      errorList.clear();
   }

   public void addError(String error)
   {
      errorList.add(error);
   }
}


 
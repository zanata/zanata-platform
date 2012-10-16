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
package org.zanata.process;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic process handle implementation that has a collection of messages to be
 * constantly fed by the running process.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class MessagesProcessHandle extends ProcessHandle
{
   public static final MessagesProcessHandle NO_HANDLE = NoProcessHandle.getNullProcessHandle(MessagesProcessHandle.class);

   private List<String> messages = new ArrayList<String>();

   public List<String> getMessages()
   {
      if( messages == null )
      {
         messages = new ArrayList<String>();
      }
      return messages;
   }

   public void setMessages(List<String> messages)
   {
      this.messages = messages;
   }

   public void addMessages(String ... messages)
   {
      for( String m : messages )
      {
         this.getMessages().add(m);
      }
   }

   public void clearMessages()
   {
      if( messages != null && messages.size() > 0 )
      {
         messages.clear();
      }
   }
}

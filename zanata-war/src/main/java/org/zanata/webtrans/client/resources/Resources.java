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
package org.zanata.webtrans.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle
{

   @Source("org/zanata/webtrans/WebTransStyles.css")
   WebTransStyles style();

   @Source("images/crystal_project/_16x16/filesystems/file_doc.png")
   ImageResource documentImage();

   @Source("images/next_entry.png")
   ImageResource nextEntry();

   @Source("images/prev_entry.png")
   ImageResource prevEntry();

   @Source("images/next_mode.png")
   ImageResource nextState();

   @Source("images/prev_mode.png")
   ImageResource prevState();

   @Source("images/first_entry.png")
   ImageResource firstEntry();

   @Source("images/last_entry.png")
   ImageResource lastEntry();

   @Source("images/loader.gif")
   ImageResource loader();

   @Source("images/loading.gif")
   ImageResource spinner();
}
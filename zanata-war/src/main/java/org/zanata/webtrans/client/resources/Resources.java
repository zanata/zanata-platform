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
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle
{

   @Source("org/zanata/webtrans/WebTransStyles.css")
   WebTransStyles style();

   @Source("images/banner_bg.png")
   DataResource bannerBackground();
   
   @Source("images/z-logo-16px.png")
   ImageResource logo();

   @Source("images/logo-small.png")
   ImageResource logo2();

   @Source("images/x.png")
   ImageResource xButton();

   @Source("images/crystal_project/_16x16/filesystems/file_doc.png")
   ImageResource documentImage();

   @Source("images/crystal_project/_16x16/actions/info.png")
   ImageResource informationImage();

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

   @Source("images/first_page.png")
   ImageResource firstPageImage();

   @Source("images/prev_page.png")
   ImageResource prevPageImage();

   @Source("images/next_page.png")
   ImageResource nextPageImage();

   @Source("images/last_page.png")
   ImageResource lastPageImage();

   @Source("images/undo.png")
   ImageResource undo();

   @Source("images/redo.png")
   ImageResource redo();

   @Source("images/undo_disabled.png")
   ImageResource undoDisabled();

   @Source("images/redo_disabled.png")
   ImageResource redoDisabled();
   
   @Source("images/loader.gif")
   ImageResource loader();

   @Source("images/loading.gif")
   ImageResource spinner();

   @Source("images/msgerror.png")
   ImageResource errorMsg();

   @Source("images/msginfo.png")
   ImageResource infoMsg();

   @Source("images/msgwarn.png")
   ImageResource warnMsg();

   @Source("images/crystal_project/_16x16/actions/view_choose.png")
   ImageResource viewChoose();
   
   @Source("images/crystal_project/_16x16/actions/help.png")
   ImageResource help();

   @Source("images/crystal_project/_16x16/actions/logout.png")
   ImageResource logout();

   @Source("images/crystal_project/_16x16/apps/kllckety.png")
   ImageResource projects();

   @Source("images/crystal_project/_16x16/apps/kdf.png")
   ImageResource groups();

   @Source("images/crystal_project/_16x16/apps/locale.png")
   ImageResource languages();

   @Source("images/crystal_project/_16x16/apps/error.png")
   ImageResource error();

   @Source("images/crystal_project/_16x16/apps/bug.png")
   ImageResource bug();
   
   @Source("images/zanata-icon/24x24/keyboard.png")
   ImageResource keyboard();

   @Source("images/zanata-icon/24x24/search.png")
   ImageResource search();

   @Source("images/zanata-icon/16x16/envelope-black.png")
   ImageResource envelopeBlack();
   
   @Source("images/zanata-icon/16x16/chevron-right.png")
   ImageResource chevronRight();

   @Source("images/zanata-icon/16x16/chevron-down.png")
   ImageResource chevronDown();

}
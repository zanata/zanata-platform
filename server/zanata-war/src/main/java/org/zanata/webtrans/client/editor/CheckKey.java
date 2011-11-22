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
package org.zanata.webtrans.client.editor;

import com.google.gwt.dom.client.NativeEvent;

/**
*
* @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
* 
* @formatter:off
* Navigation mode:
* ALT+UP arrow or J - Previous entry
* ALT+DOWN arrow or K - Next entry
* ALT+G - copy from source
* Enter - Open editor
* 
* Edit mode:
* ALT+(UP arrow/J) - Previous entry
* ALT+(DOWN arrow/K) - Next entry
* ALT+G - copy from source
* CTRL+Enter - Save as approved
* Enter - Save as approved (if isEnterKeySavesEnabled = true)
* 
**/
public interface CheckKey
{
   static final int KEY_G = 'G';
   static final int KEY_J = 'J';
   static final int KEY_K = 'K';
   static final int KEY_S = 'S';

   public static enum Context
   {
      /**
       * Edit: InlineTargetCellEditor Navigation: TableEditorPresenter
       */
      Navigation, Edit;
   }

   void init(NativeEvent event);


   /**
    * Save as fuzzy shortcut: CTRL+S
    */
   boolean isSaveAsFuzzyKey();
   
   /**
    * Move to previous state entry: ALT+PageUp
    */
   boolean isPreviousStateEntryKey();

   /**
    * Move to next state entry: ALT+PageDown
    */
   boolean isNextStateEntryKey();

   /**
    * Move to previous entry: 
    * Edit: ALT+(Up/J)
    * Navigation: ALT+Up or J
    */
   boolean isPreviousEntryKey();

   /**
    * Move to next entry: 
    * Edit: ALT+(Down/K)
    * Navigation: ALT+Down or K
    */
   boolean isNextEntryKey();

   /**
    * Copy from source: ALT+G
    * 
    */
   boolean isCopyFromSourceKey();

   /**
    * User typing in text area, any keys other than ALT+CTRL
    * 
    */
   boolean isUserTyping();
   
   /**
    * Close editor: Esc (if isEscKeyCloseEditor = true)
    * 
    */
   boolean isCloseEditorKey(boolean isEscKeyCloseEditor);

   /**
    * Is ENTER key pressed
    */
   boolean isEnterKey();

   /**
    * Save as approved key: 
    * CTRL+ENTER or 
    * ENTER (if isEnterKeySavesEnabled = true AND !shiftKey {Shift+Enter inserts new line})
    * 
    */
   boolean isSaveAsApprovedKey(boolean isEnterKeySavesEnabled);


   /**
    * Shift key presssed
    */
   boolean isShiftKey();


   /**
    * ALT key presssed
    */
   boolean isAltKey();


   /**
    * Ctrl key presssed
    */
   boolean isCtrlKey();
}


 
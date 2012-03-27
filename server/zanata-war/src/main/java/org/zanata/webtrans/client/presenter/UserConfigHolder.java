/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.client.presenter;

import java.util.Map;

import org.zanata.webtrans.client.resources.EditorConfigConstants;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;

@Singleton
public class UserConfigHolder
{
   private boolean buttonEnter = false;
   private boolean buttonEsc = false;
   private boolean buttonFuzzy = true;
   private boolean buttonUntranslated = true;
   private boolean displayButtons = true;

   public boolean isButtonEnter()
   {
      return buttonEnter;
   }

   void setButtonEnter(boolean buttonEnter)
   {
      this.buttonEnter = buttonEnter;
   }

   public boolean isButtonEsc()
   {
      return buttonEsc;
   }

   void setButtonEsc(boolean buttonEsc)
   {
      this.buttonEsc = buttonEsc;
   }

   public boolean isButtonFuzzy()
   {
      return buttonFuzzy;
   }

   void setButtonFuzzy(boolean buttonFuzzy)
   {
      this.buttonFuzzy = buttonFuzzy;
   }

   public boolean isButtonUntranslated()
   {
      return buttonUntranslated;
   }

   void setButtonUntranslated(boolean buttonUntranslated)
   {
      this.buttonUntranslated = buttonUntranslated;
   }

   public boolean isDisplayButtons()
   {
      return displayButtons;
   }

   void setDisplayButtons(boolean displayButtons)
   {
      this.displayButtons = displayButtons;
   }

   Map<String, Boolean> getConfigMap()
   {
      return ImmutableMap.<String, Boolean>builder()
            .put(EditorConfigConstants.BUTTON_ENTER, buttonEnter)
            .put(EditorConfigConstants.BUTTON_ESC, buttonEsc)
            .put(EditorConfigConstants.BUTTON_FUZZY, buttonFuzzy)
            .put(EditorConfigConstants.BUTTON_UNTRANSLATED, buttonUntranslated)
            .build();
   }
}

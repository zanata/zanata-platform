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

import org.apache.commons.beanutils.BeanUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;

public class UserConfigHolderTest
{

   private UserConfigHolder configHolder;

   @BeforeMethod
   protected void setUp() throws Exception
   {
      configHolder = new UserConfigHolder();
   }

   @Test
   public void testDefaultValues() throws Exception
   {

      Map<String, String> propertiesMap = getPropertiesMap();

      MatcherAssert.assertThat(propertiesMap, Matchers.hasEntry("buttonEnter", "false"));
      MatcherAssert.assertThat(propertiesMap, Matchers.hasEntry("buttonEsc", "false"));
      MatcherAssert.assertThat(propertiesMap, Matchers.hasEntry("buttonFuzzy", "true"));
      MatcherAssert.assertThat(propertiesMap, Matchers.hasEntry("buttonUntranslated", "true"));
      MatcherAssert.assertThat(propertiesMap, Matchers.hasEntry("displayButtons", "true"));
      MatcherAssert.assertThat(propertiesMap, Matchers.hasEntry("fuzzyAndUntranslated", "true"));
   }

   @SuppressWarnings("unchecked")
   private Map<String, String> getPropertiesMap() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      return BeanUtils.describe(configHolder);
   }

   @Test
   public void randomSetterAndGetter() throws Exception
   {
      Random random = new Random(System.nanoTime());
      boolean value = random.nextBoolean();

      configHolder.setButtonEnter(value);
      configHolder.setButtonEsc(value);
      configHolder.setButtonFuzzy(value);
      configHolder.setButtonUntranslated(value);
      configHolder.setDisplayButtons(value);

      MatcherAssert.assertThat(configHolder.isButtonEnter(), Matchers.equalTo(value));
      MatcherAssert.assertThat(configHolder.isButtonEsc(), Matchers.equalTo(value));
      MatcherAssert.assertThat(configHolder.isButtonFuzzy(), Matchers.equalTo(value));
      MatcherAssert.assertThat(configHolder.isButtonUntranslated(), Matchers.equalTo(value));
      MatcherAssert.assertThat(configHolder.isDisplayButtons(), Matchers.equalTo(value));
   }
}

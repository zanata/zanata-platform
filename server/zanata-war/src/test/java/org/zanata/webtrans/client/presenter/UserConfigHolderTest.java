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
package org.zanata.webtrans.client.presenter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.beanutils.BeanUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {"unit-tests"})
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

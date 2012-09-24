package org.zanata.webtrans.client.service;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.service.DistinctColorListImpl;
import org.zanata.webtrans.shared.auth.EditorClientId;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class DistinctColorListImplTest
{
   private static int counter = 0;
   private DistinctColorListImpl distinctColor;

   @BeforeMethod
   public void beforeMethod()
   {
      distinctColor = new DistinctColorListImpl(Lists.newArrayList("red", "blue", "green"));
   }

   private static EditorClientId newEditorClientId()
   {
      counter++;
      return new EditorClientId(String.valueOf(counter), counter);
   }

   @Test
   public void canGetNextColor()
   {
      assertThat(distinctColor.getOrCreateColor(newEditorClientId()), Matchers.is("red"));
      assertThat(distinctColor.getOrCreateColor(newEditorClientId()), Matchers.is("blue"));
      assertThat(distinctColor.getOrCreateColor(newEditorClientId()), Matchers.is("green"));
      assertThat(distinctColor.getOrCreateColor(newEditorClientId()), Matchers.is("red"));
   }

   @Test
   public void willReuseColorForSameEditorClientId()
   {
      EditorClientId editorClientId = newEditorClientId();
      String color = distinctColor.getOrCreateColor(editorClientId);
      String sameColor = distinctColor.getOrCreateColor(editorClientId);

      assertThat(sameColor, Matchers.equalTo(color));
   }

   @Test
   public void canReleaseColor()
   {
      EditorClientId editorClientId = newEditorClientId();
      String color = distinctColor.getOrCreateColor(editorClientId);
      distinctColor.releaseColor(editorClientId);
      String newColor = distinctColor.getOrCreateColor(editorClientId);

      assertThat(newColor, Matchers.not(Matchers.equalTo(color)));
   }
}

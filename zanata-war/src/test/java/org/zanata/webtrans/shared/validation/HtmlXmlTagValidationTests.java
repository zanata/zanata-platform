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
package org.zanata.webtrans.shared.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.TestMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.client.service.ValidationMessageResolverImpl;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class HtmlXmlTagValidationTests
{
   // TODO use TestMessages

   // mock message strings
   private static final String MOCK_TAGS_OUT_OF_ORDER_MESSAGE = "mock tags out of order message";
   private static final String MOCK_TAGS_MISSING_MESSAGE = "mock tags missing message";
   private static final String MOCK_TAGS_ADDED_MESSAGE = "mock tags added message";

   private HtmlXmlTagValidation htmlXmlTagValidation;

   private ValidationMessageResolver messages;

   // captured tag lists sent to messages
   @Captor
   private ArgumentCaptor<List<String>> capturedTagsAdded;
   @Captor
   private ArgumentCaptor<List<String>> capturedTagsMissing;
   @Captor
   private ArgumentCaptor<List<String>> capturedTagsOutOfOrder;

   @BeforeMethod
   public void init()
   {
      MockitoAnnotations.initMocks(this);

      messages = new ValidationMessageResolverImpl(TestMessages.getInstance(ValidationMessages.class));

      htmlXmlTagValidation = new HtmlXmlTagValidation(ValidationId.HTML_XML, messages);
      htmlXmlTagValidation.getValidationInfo().setEnabled(true);
   }

   @Test
   public void idIsSet()
   {
      assertThat(htmlXmlTagValidation.getValidationInfo().getId(), is(ValidationId.HTML_XML));
   }

   @Test
   public void matchingHtmlNoError()
   {
      String source = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      String target = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(false));
      assertThat(htmlXmlTagValidation.getError().size(), is(0));
   }

   @Test
   public void matchingXmlNoError()
   {
      String source = "<group><users><user>name</user></users></group>";
      String target = "<group><users><user>nombre</user></users></group>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(false));
      assertThat(htmlXmlTagValidation.getError().size(), is(0));
   }

   @Test
   public void addedTagError()
   {
      String source = "<group><users><user>1</user></users></group>";
      String target = "<group><users><user>1</user></users><foo></group>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_ADDED_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsAdded.getValue(), hasItem("<foo>"));
      assertThat(capturedTagsAdded.getValue().size(), is(1));
   }

   @Test
   public void addedTagsError()
   {
      String source = "<group><users><user>1</user></users></group>";
      String target = "<foo><group><users><bar><user>1</user></users></group><moo>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_ADDED_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsAdded.getValue(), hasItems("<foo>", "<bar>", "<moo>"));
      assertThat(capturedTagsAdded.getValue().size(), is(3));
   }

   @Test
   public void missingTagError()
   {
      String source = "<html><title>HTML TAG Test</title><foo><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      String target = "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_MISSING_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsMissing.getValue(), hasItem("<foo>"));
      assertThat(capturedTagsMissing.getValue().size(), is(1));
   }

   @Test
   public void missingTagsError()
   {
      String source = "<html><title>HTML TAG Test</title><p><table><tr><td>column 1 row 1</td></tr></table></html>";
      String target = "<title>HTML TAG Test</title><table><tr><td>column 1 row 1</td></tr></table>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_MISSING_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsMissing.getValue(), hasItems("<html>", "<p>", "</html>"));
      assertThat(capturedTagsMissing.getValue().size(), is(3));
   }

   @Test
   public void orderOnlyValidatedWithSameTags()
   {
      String source = "<one><two><three></four></five>";
      String target = "<two></five></four><three><six>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_MISSING_MESSAGE));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_ADDED_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(2));

      assertThat(capturedTagsMissing.getValue(), hasItem("<one>"));
      assertThat(capturedTagsMissing.getValue().size(), is(1));
      assertThat(capturedTagsAdded.getValue(), hasItem("<six>"));
      assertThat(capturedTagsAdded.getValue().size(), is(1));
   }

   @Test
   public void lastTagMovedToFirstError()
   {
      String source = "<one><two><three></four></five><six>";
      String target = "<six><one><two><three></four></five>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsOutOfOrder.getValue(), hasItem("<six>"));
      assertThat("when one tag has moved, only that tag should be reported out of order", capturedTagsOutOfOrder.getValue().size(), is(1));
   }

   @Test
   public void firstTagMovedToLastError()
   {
      String source = "<one><two><three></four></five><six>";
      String target = "<two><three></four></five><six><one>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsOutOfOrder.getValue(), hasItem("<one>"));
      assertThat("when one tag has moved, only that tag should be reported out of order", capturedTagsOutOfOrder.getValue().size(), is(1));
   }

   @Test
   public void tagMovedToMiddleError()
   {
      String source = "<one><two><three></four></five><six>";
      String target = "<two><three><one></four></five><six>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsOutOfOrder.getValue(), hasItem("<one>"));
      assertThat("when one tag has moved, only that tag should be reported out of order", capturedTagsOutOfOrder.getValue().size(), is(1));
   }

   @Test
   public void reversedTagsError()
   {
      String source = "<one><two><three></four></five><six>";
      String target = "<six></five></four><three><two><one>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      // <one> is the first in-order tag, so is not reported
      assertThat(capturedTagsOutOfOrder.getValue(), hasItems("<six>", "</five>", "</four>", "<three>", "<two>"));
      assertThat(capturedTagsOutOfOrder.getValue().size(), is(5));
   }

   @Test
   public void reportFirstTagsOutOfOrder()
   {
      String source = "<one><two><three></four></five><six>";
      String target = "</four></five><six><one><two><three>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsOutOfOrder.getValue(), hasItems("</four>", "</five>", "<six>"));
      assertThat(capturedTagsOutOfOrder.getValue().size(), is(3));
   }

   @Test
   public void reportLeastTagsOutOfOrder()
   {
      String source = "<one><two><three></four></five><six>";
      String target = "<six></four></five><one><two><three>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      // <one><two><three> in order
      // should not use </four></five> as there are less tags
      assertThat("should report the least number of tags to move to restore order", capturedTagsOutOfOrder.getValue(), hasItems("</four>", "</five>", "<six>"));
      assertThat(capturedTagsOutOfOrder.getValue().size(), is(3));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void swapSomeTagsError()
   {
      String source = "<one><two><three></three></two><four></four></one>";
      String target = "<one><two></two><four></three><three></four></one>";
      htmlXmlTagValidation.validate(source, target);

      assertThat(htmlXmlTagValidation.hasError(), is(true));
      assertThat(htmlXmlTagValidation.getError(), hasItem(MOCK_TAGS_OUT_OF_ORDER_MESSAGE));
      assertThat(htmlXmlTagValidation.getError().size(), is(1));

      assertThat(capturedTagsOutOfOrder.getValue(), hasItems("<three>", "</three>"));
      assertThat(capturedTagsOutOfOrder.getValue(), not(anyOf(hasItem("<one>"), hasItem("<two>"), hasItem("</two>"), hasItem("<four>"), hasItem("</four>"), hasItem("</one>"))));
      assertThat(capturedTagsOutOfOrder.getValue().size(), is(2));
   }
}


 
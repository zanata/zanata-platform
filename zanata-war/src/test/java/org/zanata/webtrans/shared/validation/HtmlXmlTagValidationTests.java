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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class HtmlXmlTagValidationTests {
    private HtmlXmlTagValidation htmlXmlTagValidation;

    private ValidationMessages messages;

    @BeforeMethod
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);

        messages = Gwti18nReader.create(ValidationMessages.class);

        htmlXmlTagValidation =
                new HtmlXmlTagValidation(ValidationId.HTML_XML, messages);
        htmlXmlTagValidation.getRules().setEnabled(true);
    }

    @Test
    public void idIsSet() {
        assertThat(htmlXmlTagValidation.getId(), is(ValidationId.HTML_XML));
    }

    @Test
    public void matchingHtmlNoError() {
        String source =
                "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        String target =
                "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList.size(), is(0));
    }

    @Test
    public void matchingXmlNoError() {
        String source = "<group><users><user>name</user></users></group>";
        String target = "<group><users><user>nombre</user></users></group>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList.size(), is(0));
    }

    @Test
    public void addedTagError() {
        String source = "<group><users><user>1</user></users></group>";
        String target = "<group><users><user>1</user></users><foo></group>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList,
                hasItem(messages.tagsAdded(Arrays.asList("<foo>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void addedTagsError() {
        String source = "<group><users><user>1</user></users></group>";
        String target =
                "<foo><group><users><bar><user>1</user></users></group><moo>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList, hasItem(messages.tagsAdded(Arrays.asList("<foo>",
                "<bar>", "<moo>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void missingTagError() {
        String source =
                "<html><title>HTML TAG Test</title><foo><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        String target =
                "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList,
                hasItem(messages.tagsMissing(Arrays.asList("<foo>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void missingTagsError() {
        String source =
                "<html><title>HTML TAG Test</title><p><table><tr><td>column 1 row 1</td></tr></table></html>";
        String target =
                "<title>HTML TAG Test</title><table><tr><td>column 1 row 1</td></tr></table>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList, hasItem(messages.tagsMissing(Arrays.asList(
                "<html>", "<p>", "</html>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void orderOnlyValidatedWithSameTags() {
        String source = "<one><two><three></four></five>";
        String target = "<two></five></four><three><six>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList,
                hasItem(messages.tagsMissing(Arrays.asList("<one>"))));
        assertThat(errorList,
                hasItem(messages.tagsAdded(Arrays.asList("<six>"))));
        assertThat(errorList.size(), is(2));
    }

    @Test
    public void lastTagMovedToFirstError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<six><one><two><three></four></five>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList,
                hasItem(messages.tagsWrongOrder(Arrays.asList("<six>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void firstTagMovedToLastError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<two><three></four></five><six><one>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList,
                hasItem(messages.tagsWrongOrder(Arrays.asList("<one>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void tagMovedToMiddleError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<two><three><one></four></five><six>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList,
                hasItem(messages.tagsWrongOrder(Arrays.asList("<one>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void reversedTagsError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<six></five></four><three><two><one>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList, hasItem(messages.tagsWrongOrder(Arrays.asList(
                "<two>", "<three>", "</four>", "</five>", "<six>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void reportFirstTagsOutOfOrder() {
        String source = "<one><two><three></four></five><six>";
        String target = "</four></five><six><one><two><three>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList, hasItem(messages.tagsWrongOrder(Arrays.asList(
                "</four>", "</five>", "<six>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void reportLeastTagsOutOfOrder() {
        String source = "<one><two><three></four></five><six>";
        String target = "<six></four></five><one><two><three>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList, hasItem(messages.tagsWrongOrder(Arrays.asList(
                "</four>", "</five>", "<six>"))));
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void swapSomeTagsError() {
        String source = "<one><two><three></three></two><four></four></one>";
        String target = "<one><two></two><four></three><three></four></one>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList, hasItem(messages.tagsWrongOrder(Arrays.asList(
                "<three>", "</three>"))));
        assertThat(errorList.size(), is(1));
    }
}

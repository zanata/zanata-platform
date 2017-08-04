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

import static java.util.Arrays.asList;
import static org.zanata.webtrans.shared.validation.FakeValidationMessages.fakeValidationMessages;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.HtmlXmlTagValidation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class HtmlXmlTagValidationTests {
    private HtmlXmlTagValidation htmlXmlTagValidation;

    private ValidationMessages messages;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        messages = fakeValidationMessages();
        htmlXmlTagValidation =
                new HtmlXmlTagValidation(ValidationId.HTML_XML, messages);
        htmlXmlTagValidation.getRules().setEnabled(true);
    }

    @Test
    public void idIsSet() {
        assertThat(htmlXmlTagValidation.getId()).isEqualTo(ValidationId.HTML_XML);
    }

    @Test
    public void matchingHtmlNoError() {
        String source =
                "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        String target =
                "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void matchingXmlNoError() {
        String source = "<group><users><user>name</user></users></group>";
        String target = "<group><users><user>nombre</user></users></group>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void addedTagError() {
        String source = "<group><users><user>1</user></users></group>";
        String target = "<group><users><user>1</user></users><foo></group>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsAdded(asList("<foo>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void addedTagsError() {
        String source = "<group><users><user>1</user></users></group>";
        String target =
                "<foo><group><users><bar><user>1</user></users></group><moo>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsAdded(asList("<foo>",
                "<bar>", "<moo>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void missingTagError() {
        String source =
                "<html><title>HTML TAG Test</title><foo><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        String target =
                "<html><title>HTML TAG Test</title><table><tr><td>column 1 row 1</td><td>column 2 row 1</td></tr></table></html>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsMissing(asList("<foo>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void missingTagsError() {
        String source =
                "<html><title>HTML TAG Test</title><p><table><tr><td>column 1 row 1</td></tr></table></html>";
        String target =
                "<title>HTML TAG Test</title><table><tr><td>column 1 row 1</td></tr></table>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsMissing(asList(
                "<html>", "<p>", "</html>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void orderOnlyValidatedWithSameTags() {
        String source = "<one><two><three></four></five>";
        String target = "<two></five></four><three><six>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsMissing(asList("<one>")));
        assertThat(errorList).contains(messages.tagsAdded(asList("<six>")));
        assertThat(errorList.size()).isEqualTo(2);
    }

    @Test
    public void lastTagMovedToFirstError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<six><one><two><three></four></five>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList("<six>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void firstTagMovedToLastError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<two><three></four></five><six><one>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList("<one>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void tagMovedToMiddleError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<two><three><one></four></five><six>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList("<one>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void reversedTagsError() {
        String source = "<one><two><three></four></five><six>";
        String target = "<six></five></four><three><two><one>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList(
                "<two>", "<three>", "</four>", "</five>", "<six>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void reportFirstTagsOutOfOrder() {
        String source = "<one><two><three></four></five><six>";
        String target = "</four></five><six><one><two><three>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList(
                "</four>", "</five>", "<six>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void reportLeastTagsOutOfOrder() {
        String source = "<one><two><three></four></five><six>";
        String target = "<six></four></five><one><two><three>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList(
                "</four>", "</five>", "<six>")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void swapSomeTagsError() {
        String source = "<one><two><three></three></two><four></four></one>";
        String target = "<one><two></two><four></three><three></four></one>";
        List<String> errorList = htmlXmlTagValidation.validate(source, target);

        assertThat(errorList).contains(messages.tagsWrongOrder(asList(
                "<three>", "</three>")));
        assertThat(errorList.size()).isEqualTo(1);
    }
}

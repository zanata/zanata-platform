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

import static org.zanata.webtrans.shared.validation.FakeValidationMessages.fakeValidationMessages;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.XmlEntityValidation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class XMLEntityValidationTest {
    private XmlEntityValidation xmlEntityValidation;

    private ValidationMessages messages;

    @Before
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);
        messages = fakeValidationMessages();

        xmlEntityValidation =
                new XmlEntityValidation(ValidationId.XML_ENTITY, messages);
        xmlEntityValidation.getRules().setEnabled(true);
    }

    @Test
    public void idIsSet() {
        assertThat(xmlEntityValidation.getId()).isEqualTo(ValidationId.XML_ENTITY);
    }

    @Test
    public void testNoEntity() {
        String source = "Source string without xml entity";
        String target = "Target string without xml entity";
        List<String> errorList = xmlEntityValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void testWithCompleteEntity() {
        String source = "Source string";
        String target = "Target string: &mash; bla bla &test;";
        List<String> errorList = xmlEntityValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void testWithIncompleteEntityCharRef() {
        String source = "Source string";
        String target = "Target string: &mash bla bla &test";
        List<String> errorList = xmlEntityValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(2);
        assertThat(errorList).contains(messages.invalidXMLEntity("&mash"),
                messages.invalidXMLEntity("&test"));
    }

    @Test
    public void testWithIncompleteEntityDecimalRef() {
        String source = "Source string";
        String target = "Target string: &#1234 bla bla &#BC;";
        List<String> errorList = xmlEntityValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(2);
        assertThat(errorList).contains(messages.invalidXMLEntity("&#1234"),
                messages.invalidXMLEntity("&#BC;"));
    }

    @Test
    public void testWithIncompleteEntityHexadecimalRef() {
        String source = "Source string";
        String target = "Target string: &#x1234 bla bla &#x09Z";
        List<String> errorList = xmlEntityValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(2);
        assertThat(errorList).contains(messages.invalidXMLEntity("&#x1234"),
                messages.invalidXMLEntity("&#x09Z"));
    }

}

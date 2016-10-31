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
package org.zanata.webtrans.shared.validation.action;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.AbstractValidationAction;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class NewlineLeadTrailValidation extends AbstractValidationAction {
    public NewlineLeadTrailValidation(ValidationId id,
            ValidationMessages messages) {
        super(id, messages.newLineValidatorDesc(), messages);
    }

    private final static String leadNewlineRegex = "^\n";
    private final static String endNewlineRegex = "\n$";
    private final static String newlineRegex = "\n";

    private final static RegExp leadRegExp = RegExp.compile(leadNewlineRegex);
    private final static RegExp endRegExp = RegExp.compile(endNewlineRegex);

    @Override
    public List<String> doValidate(String source, String target) {
        ArrayList<String> errors = new ArrayList<String>();

        if (notShareLeading(source, target)) {
            errors.add(getMessages().leadingNewlineMissing());
        }

        if (notShareLeading(target, source)) {
            errors.add(getMessages().leadingNewlineAdded());
        }

        if (notShareTrailing(source, target)) {
            errors.add(getMessages().trailingNewlineMissing());
        }

        if (notShareTrailing(target, source)) {
            errors.add(getMessages().trailingNewlineAdded());
        }

        return errors;
    }

    private boolean notShareTrailing(String source, String target) {
        return !shareTrailing(source, target);
    }

    private boolean notShareLeading(String source, String target) {
        return !shareLeading(source, target);
    }

    /**
     * @return false if base has a leading newline and test does not, true
     *         otherwise
     */
    private boolean shareLeading(String base, String test) {
        if (leadRegExp.test(base)) {
            return leadRegExp.test(test);
        }
        // no newline so can't fail
        return true;
    }

    /**
     * @return false if base has a trailing newline and test does not, true
     *         otherwise
     */
    private boolean shareTrailing(String base, String test) {
        if (endRegExp.test(base)) {
            return endRegExp.test(test);
        }
        // no newline so can't fail
        return true;
    }

    @Override
    public String getSourceExample() {
        return "\\n hello world with lead new line";
    }

    @Override
    public String getTargetExample() {
        return "<span class='js-example__target txt--warning'>missing \\n</span> hello world with lead new line";
    }
}

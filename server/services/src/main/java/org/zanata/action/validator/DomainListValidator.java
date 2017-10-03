/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.action.validator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DomainListValidator implements ConstraintValidator<DomainList, String> {

    @Override
    public void initialize(DomainList annotation) {
    }

    @Override
    public boolean isValid(String value,
            ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        List<String> domains = Arrays.asList(value.split(","));
        domains = domains.stream().map(String::trim)
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.toList());
        // domain validation is pretty complicated http://stackoverflow.com/questions/10306690/domain-name-validation-with-regex
        // here we just take a quick win...
        EmailValidator emailValidator = new EmailValidator();
        return domains.stream().map(domain -> "user@" + domain)
                .allMatch(email -> emailValidator.isValid(email,
                        context));
    }
}

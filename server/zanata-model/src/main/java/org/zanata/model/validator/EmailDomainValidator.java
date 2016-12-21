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
package org.zanata.model.validator;

import java.io.Serializable;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EmailDomainValidator
        implements ConstraintValidator<EmailDomain, String>,
        Serializable {

    private Provider<Set<String>> permittedEmailDomainsProvider;

    @Inject
    public EmailDomainValidator(@AcceptedEmailDomainsForNewAccount
            Provider<Set<String>> permittedEmailDomainsProvider) {
        this.permittedEmailDomainsProvider = permittedEmailDomainsProvider;
    }

    @Override
    public void initialize(EmailDomain emailDomain) {
    }

    @Override
    public boolean isValid(String email,
            ConstraintValidatorContext constraintValidatorContext) {
        if (Strings.isNullOrEmpty(email) || email.trim().length() == 0) {
            return true;
        }
        Set<String> emailDomains = permittedEmailDomainsProvider.get();
        if (emailDomains.isEmpty()) {
            return true;
        }
        return emailDomains.stream()
                .anyMatch(domain -> matchDomain(email, domain));
    }

    private static boolean matchDomain(String email, String domain) {
        return email.endsWith("@" + domain);
    }
}

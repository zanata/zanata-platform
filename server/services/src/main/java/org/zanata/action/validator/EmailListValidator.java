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
package org.zanata.action.validator;

import java.io.Serializable;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.model.validator.ZanataEmail;
import org.zanata.model.validator.ZanataEmailValidator;

@ApplicationScoped
public class EmailListValidator implements
        ConstraintValidator<EmailList, String>, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings("SE_BAD_FIELD")
    @Inject
    private Validator validator;

    private final ZanataEmailValidator zanataEmailValidator =
            new ZanataEmailValidator();

    private static class EmailHolder {
        EmailHolder(String email) {
            this.email = email;
        }
        final @ZanataEmail
        String email;
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null || s.isEmpty()) {
            return true;
        }

        // trim still required to prevent leading whitespace invalidating the
        // first email address
        for (String email : s.trim().split("\\s*,\\s*")) {
            if (!zanataEmailValidator.isValid(email, context)) {
                return false;
            }

            Set<?> violations = validator.validate(new EmailHolder(email));
            if (!violations.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void initialize(EmailList parameters) {
    }

}

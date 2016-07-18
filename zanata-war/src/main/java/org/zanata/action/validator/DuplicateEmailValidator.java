/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.zanata.dao.PersonDAO;

public class DuplicateEmailValidator implements
        ConstraintValidator<NotDuplicateEmail, String>, Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PersonDAO personDAO;

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context) {
        if (string == null) {
            return true;
        }
        if (string.length() == 0) {
            return true;
        }
        return personDAO.findByEmail(string) == null;
    }

    @Override
    public void initialize(NotDuplicateEmail parameters) {
    }

}

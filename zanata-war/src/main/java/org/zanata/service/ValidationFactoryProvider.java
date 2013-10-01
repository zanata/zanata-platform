/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.service;

import java.io.IOException;

import org.zanata.exception.ValidationException;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.validation.ValidationFactory;

/**
 * Provide new instance of ValidationFactory Used on server side -
 * org.zanata.webtrans.client.resources.ValidationMessages is client side
 * localisation. used Gwti18nReader as a basic parser for ValidationMessages.
 */
public final class ValidationFactoryProvider {
    private static ValidationFactory validationFactory;

    public static ValidationFactory getFactoryInstance() {
        if (validationFactory == null) {
            try {
                ValidationMessages valMessages =
                        Gwti18nReader.create(ValidationMessages.class);
                validationFactory = new ValidationFactory(valMessages);
            } catch (IOException e) {
                throw new ValidationException(
                        "Unable to load validation messages");
            }
        }
        return validationFactory;
    }
}

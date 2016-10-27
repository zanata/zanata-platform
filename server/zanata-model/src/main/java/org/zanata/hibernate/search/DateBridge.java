/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.hibernate.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.search.bridge.TwoWayStringBridge;

public class DateBridge implements TwoWayStringBridge {

    // TODO include milliseconds for more precision (".SSS")
    private final static String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            DATE_FORMAT);

    @Override
    public String objectToString(Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            return dateFormat.format(date);
        } else {
            throw new IllegalArgumentException(
                    "DateBridge used on a non-Date type: " + value.getClass());
        }
    }

    @Override
    public Object stringToObject(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "DateBridge used on invalid date format: " + date);
        }
    }

}

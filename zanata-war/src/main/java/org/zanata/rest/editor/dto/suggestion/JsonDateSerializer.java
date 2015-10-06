/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.editor.dto.suggestion;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Serializer to output dates in ISO-8601 format.
 *
 * This format is used for the JSON API because it is compatible with
 * JavaScript Date.parse() and is a widely used standard.
 */
public class JsonDateSerializer extends JsonSerializer<Date> {

    private static final DateTimeFormatter ISO8601Format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZONE);
        String dateString = ISO8601Format.format(ldt);
        jsonGenerator.writeString(dateString);
    }

}

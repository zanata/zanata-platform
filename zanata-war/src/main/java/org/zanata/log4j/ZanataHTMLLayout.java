/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.log4j;

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Extension of Log4J's HTML layout.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataHTMLLayout extends HTMLLayout
{
   @Override
   public String format(LoggingEvent event)
   {
      StringBuilder builder = new StringBuilder();

      // add a row with the username
      builder.append("<tr>");
      builder.append("<td>");
      builder.append("Username: ");
      builder.append("</td>");
      builder.append("<td colspan='5'>");
      builder.append(event.getMDC("username"));
      builder.append("</td>");
      builder.append("</tr>");

      builder.append(super.format(event));
      return builder.toString();
   }
}

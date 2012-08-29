/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.adapter;

//import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.openoffice.OpenOfficeFilter;

/**
 * Adapter to handle any Open Document Format documents (used by LibreOffice and OpenOffice).
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class OpenOfficeAdapter extends GenericOkapiFilterAdapter
{
   public OpenOfficeAdapter()
   {
      super(prepareFilter(), IdSource.textUnitId, true, true);
   }

   private static OpenOfficeFilter prepareFilter()
   {
      // FIXME this is an attempt to prevent escaping of translation XML, e.g. the XML for a picture
      // copied exactly from source is escaped and shows up as XML in the translated document instead
      // of as a picture.
      // Unfortunately there is no "escapeLT" param, which means it will always escape the opening
      // of XML tags and can therefore never work properly with a normal string unless I can override
      // the escaping mechanism.
      OpenOfficeFilter filter = new OpenOfficeFilter();
//      IParameters params = filter.getParameters();
//      params.setBoolean("escapeGT", false);
//      params.setBoolean("escapeNbsp", false);
//      params.setBoolean("escapeLineBreak", false);
//      params.setBoolean("quoteModeDefined", true);
//      // quote mode 0 uses literal " and /, others seem to use different quoting mechanisms
//      params.setInteger("quoteMode", 0);
//      filter.setParameters(params);
      return filter;
   }
}

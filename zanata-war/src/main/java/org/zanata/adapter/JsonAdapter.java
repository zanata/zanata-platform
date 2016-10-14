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
package org.zanata.adapter;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.json.JSONFilter;
import net.sf.okapi.filters.json.Parameters;

/**
 * Adapter to handle JavaScript Object Notation (JSON) documents.
 * It uses the Okapi's {@link net.sf.okapi.filters.json.JSONFilter} class.
 * @see <a href="http://www.json.org/">JSON Specification</a>
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class JsonAdapter extends OkapiFilterAdapter {

    public JsonAdapter() {
        super(prepareFilter(), IdSource.contentHash, true, true);
    }

    private static JSONFilter prepareFilter() {
        return new JSONFilter();
    }

    @Override
    protected void updateParamsWithDefaults(IParameters params) {
        Parameters p = (Parameters) params;
        p.fromString("extractIsolatedStrings.b=true");
    }
}

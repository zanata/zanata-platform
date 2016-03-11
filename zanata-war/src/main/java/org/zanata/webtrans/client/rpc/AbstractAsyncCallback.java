/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.rpc;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T> {
    @Override
    public void onFailure(Throwable caught) {
        String className = getClass().getSimpleName();
        if (caught instanceof StatusCodeException) {
            StatusCodeException sce = (StatusCodeException) caught;
            if (sce.getStatusCode() == 0) {
                // This is typically caused by navigating away from
                // the GWT page, which cancels any outstanding AJAX calls
                // with code 0.
                // See https://code.google.com/p/google-web-toolkit/issues/detail?id=2858
                Log.info("GWT-RPC response code: 0 for " + "class " +
                        className);
                return;
            }
        }
        Log.error("Async callback failure for class " + className, caught);
    }

}

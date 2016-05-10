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
package org.zanata.servlet;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.addAll;
import static javax.servlet.DispatcherType.*;

/**
 * Removes duplicated values for each parameter name (multiple values are
 * preserved in order, as long as the values are diferent). Intended only as
 * a workaround for https://github.com/ocpsoft/rewrite/issues/223
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@WebFilter(filterName = "DuplicateParamFilter", urlPatterns = "/*", dispatcherTypes = {
        FORWARD,
        REQUEST,
        INCLUDE,
        ASYNC,
        ERROR})
public class DuplicateParamFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new FilteredRequest(request), response);
    }

    static class FilteredRequest extends HttpServletRequestWrapper {
        private @Nullable Map<String, String[]> lazyMap;
        public FilteredRequest(ServletRequest request) {
            super((HttpServletRequest) request);
        }

        @Override
        public @Nullable String getParameter(String paramName) {
            return getRequest().getParameter(paramName);
        }

        // Remove duplicate strings, preserving order
        @Nonnull static String[] deduplicate(@Nonnull String[] values) {
            // premature optimisation:
            if (values.length < 2) {
                return values;
            }
            LinkedHashSet<String> set = new LinkedHashSet<>();
            addAll(set, values);
            return set.toArray(new String[set.size()]);
        }

        @Override
        public @Nullable String[] getParameterValues(String paramName) {
            @Nullable String[] values = getRequest().getParameterValues(paramName);
            if (values == null) {
                return null;
            }
            return deduplicate(values);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (lazyMap == null) {
                Map<String, String[]> origMap = getRequest().getParameterMap();
                if (origMap.isEmpty()) {
                    lazyMap = ImmutableMap.of();
                } else {
                    ImmutableMap.Builder<String, String[]> mapBuilder =
                            ImmutableMap.builder();
                    for (Entry<String, String[]> e : origMap.entrySet()) {
                        String key = e.getKey();
                        String[] values = e.getValue();
                        if (values != null) {
                            mapBuilder.put(key, deduplicate(values));
                        }
                    }
                    lazyMap = mapBuilder.build();
                }
            }
            return lazyMap;
        }
    }

}

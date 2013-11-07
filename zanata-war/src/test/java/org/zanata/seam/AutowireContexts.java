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
package org.zanata.seam;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AutowireContexts {

    public enum ContextType {
        // TODO implement other contexts as required
        Request, Session;
    }

    private static final AutowireContexts instance = new AutowireContexts();

    private final Map<ContextType, Map<String, Object>> allContexts =
            new HashMap<ContextType, Map<String, Object>>();

    private AutowireContexts() {
        // By default, operate with a new request, and a new session
        newSession();
    }

    public static AutowireContexts getInstance() {
        return instance;
    }

    /**
     * Returns a Seam Autowire scope context.
     *
     * @param contextType
     *            The type of context
     * @return A seam Autowire Context for the given scope. Could be null.
     */
    public Map<String, Object> getContext(ContextType contextType) {
        return ImmutableMap.copyOf(allContexts.get(contextType));
    }

    /**
     * Searches for a value in all available contexts. It will search in lower
     * scoped contexts first and then move above (as Seam does).
     *
     * @param name
     *            Name of the value
     * @return The value, if found in any context. Null otherwise.
     */
    public Object getValue(String name) {
        Map<String, Object> requestCtx = allContexts.get(ContextType.Request);
        if (requestCtx != null && requestCtx.containsKey(name)) {
            return requestCtx.get(name);
        }

        Map<String, Object> sessionCtx = allContexts.get(ContextType.Session);
        if (sessionCtx != null && sessionCtx.containsKey(name)) {
            return sessionCtx.get(name);
        }

        // not found in any context
        return null;
    }

    public void putValue(String name, ContextType ctx, Object value) {
        Map<String, Object> context = allContexts.get(ctx);
        if (context == null) {
            throw new RuntimeException("Context of Type " + ctx.name()
                    + " is not available. Check the SeamAutowire setup.");
        } else {
            context.put(name, value);
        }
    }

    public void newRequest() {
        // clear out the current request context
        allContexts.put(ContextType.Request, new HashMap<String, Object>());
    }

    public void newSession() {
        // Clear out the request and session contexts
        newRequest();
        allContexts.put(ContextType.Session, new HashMap<String, Object>());
    }

}

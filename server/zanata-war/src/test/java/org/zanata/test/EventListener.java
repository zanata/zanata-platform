/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.test;

import com.google.common.collect.Lists;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A test event listener to capture all fired events and make assertions
 * about them.
 * To use this listener, simply inject it into a CdiUnit test and then use
 * its methods to query on the fired events.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@ApplicationScoped
public class EventListener {

    private List firedEvents = new ArrayList<>();

    public void fireEvent(@Observes Object event) {
        firedEvents.add(event);
    }

    public List<Object> getFiredEvents() {
        return Lists.newArrayList(firedEvents);
    }

    public <T> List<T> getFiredEvents(Class<T> eventType) {
        return (List<T>) firedEvents.stream()
                .filter(e -> eventType.isInstance(e))
                .collect(Collectors.toList());
    }
}

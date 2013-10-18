/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.client.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "hookType", propOrder = { "befores", "afters" })
public class CommandHook implements Serializable {

    private static final long serialVersionUID = 1L;

    private String command;
    private List<String> before = new ArrayList<String>();
    private List<String> after = new ArrayList<String>();

    @XmlAttribute(name = "command", required = true)
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @XmlElements({ @XmlElement(name = "before", type = String.class) })
    public List<String> getBefores() {
        return before;
    }

    @XmlElements({ @XmlElement(name = "after", type = String.class) })
    public List<String> getAfters() {
        return after;
    }

    @Override
    public String toString() {
        StringBuilder sb =
                new StringBuilder("hook{ before-").append(command).append("[");
        for (String bef : before) {
            sb.append("\"").append(bef).append("\",");
        }
        sb.append("], after-").append(command).append("[");
        for (String aft : after) {
            sb.append("\"").append(aft).append("\",");
        }
        return sb.append("] }").toString();
    }
}

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

/**
 * <p>Optional element used to attach system commands to run before or after a
 * Zanata command (the "hooked command"). The hooked command is specified in the
 * "command" attribute.</p>
 *
 * <p>Include {@code <hook>} elements within a {@code <hooks>} element in the
 * {@code <config>} element of zanata.xml. Each command should have zero or one
 * hooks. Each hook can have any number of {@code <before>} and {@code <after>}
 * elements.</p>
 *
 * <p>Commands specified in {@code <before>} elements will be run before the hooked
 * command, in the order that they are specified. Commands specified in
 * {@code <after>} elements are similarly run in order after the hooked command
 * successfully completes. If any command fails, including the hooked command,
 * no further commands will be run.</p>
 *
 * e.g.
 *
 * <pre>
 * {@code
 * <hooks>
 *   <hook command="push">
 *     <before>po4a-gettextize -f man -m manpage.1 -p manpage.pot</before>
 *     <after>rm -f manpage.pot</after>
 *   </hook>
 *   <hook command="pull">
 *     <after>po4a-translate -f man -m manpage.1 -p trans/de/manpage.po -l manpage.de.1 --keep 1</after>
 *     <after>rm -rf trans</after>
 *   </hook>
 * </hooks>
 * }
 * </pre>
 */
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

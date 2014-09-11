/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.client.config;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@XmlType(name = "fileMappingRule")
@XmlRootElement(name = "rule")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class FileMappingRule {
    private String pattern;
    private String rule;

    public FileMappingRule() {
    }

    public FileMappingRule(String pattern, String rule) {
        this.pattern = pattern;
        this.rule = rule;
    }

    @XmlAttribute(name = "apply-to", required = false)
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    public String getPattern() {
        return pattern;
    }

    @XmlValue
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    public String getRule() {
        return rule;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}

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

import org.zanata.client.commands.DocNameWithExt;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * If your project has single type of source documents, you can omit
 * the pattern. It will then map everything belong to that project type with this
 * rule. This is typically used for non-file type projects.
 * <br />
 * For example for a "gettext" type project, all pot file will be mapped:<br />
 * {@code <rule>{path}/{locale_with_underscore}.po</rule>}
 * <p>
 * To map multiple file types in a "file" type project, you can use pattern to
 * set individual rule(s):
 * <br />
 * {@code <rule pattern="**&#47*.odt">{path}/{locale}/{filename}.{extension}</rule> }
 * {@code <rule pattern="**&#47*.idml">output/{path}/{locale}/{filename}.{extension}</rule> }
 * <p>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@XmlType(name = "fileMappingRule")
@XmlRootElement(name = "rule")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class FileMappingRule implements Serializable {
    private String pattern;
    private String rule;

    public FileMappingRule() {
    }

    public FileMappingRule(String pattern, String rule) {
        this.pattern = pattern;
        this.rule = rule;
    }

    /**
     * If pattern is missing, this rule will be applied to matching file types.
     *
     * @see org.zanata.client.commands.FileMappingRuleHandler#isApplicable(DocNameWithExt)
     */
    public FileMappingRule(String rule) {
        this.rule = rule;
    }

    /**
     * Represents glob pattern to files that are applicable for this rule.
     */
    @XmlAttribute(name = "pattern", required = false)
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
    public String getPattern() {
        return pattern;
    }

    /**
     * Represents the actual mapping rule.
     */
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

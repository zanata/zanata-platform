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
package org.zanata.action;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("javaScriptTestHelper")
@Dependent
public class JavaScriptTestHelper {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JavaScriptTestHelper.class);

    private static final String propName = "zanata.javaScriptTestHelper";
    private static final String HEAD_SCRIPT =
            "<script type=\"application/javascript\">\nwindow.addEventListener(\'error\', function (e) {\n  // For some reason, this form (with a comma) won\'t let WebDriver see the stack trace:\n  // console.error(\'error stack:\', e.error.stack.toString());\n  console.error(\'error stack: \' + e.error.stack.toString());\n});\n</script>\n";

    public String getHeadScript() {
        if (Boolean.getBoolean(propName)) {
            return HEAD_SCRIPT;
        }
        return "";
    }
}

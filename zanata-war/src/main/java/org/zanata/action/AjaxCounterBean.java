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

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

@Named("ajaxCounter")
@Slf4j
public class AjaxCounterBean {
    // http://stackoverflow.com/questions/4410218/trying-to-keep-track-of-number-of-outstanding-ajax-requests-in-firefox
    private static final String AJAX_COUNTER_SCRIPT = "<script type=\"application/javascript\">" +
            "(function(xhr) {\n" +
            "  if (xhr.active === undefined) {\n" +
            "    xhr.active = 0;\n" +
            "    var pt = xhr.prototype;\n" +
            "    pt._send = pt.send;\n" +
            "    pt.send = function() {\n" +
            "        XMLHttpRequest.active++;\n" +
            "        this._onreadystatechange = this.onreadystatechange;\n" +
            "        this.onreadystatechange = function(e) {\n" +
            "            if ( this._onreadystatechange ) {\n" +
            "                var fn = this._onreadystatechange.handleEvent || this._onreadystatechange;\n" +
            "                fn.apply(this, arguments);\n" +
            "            }\n" +
            "            if ( this.readyState == 4 ) {\n" +
            "                XMLHttpRequest.active--;\n" +
            "            }\n" +
            "        };\n" +
            "        this._send.apply(this, arguments);\n" +
            "    }\n" +
            "    window.timeoutCounter = 0;\n" +
            "    window.originalSetTimeout = window.setTimeout;\n" +
            "    window.setTimeout = function(func, delay, params) {\n" +
            "        window.timeoutCounter++;\n" +
            "        window.originalSetTimeout(window.timeoutCallback, delay, [func, params]);\n" +
            "    }\n" +
            "    window.timeoutCallback = function(funcAndParams) {\n" +
            "        window.timeoutCounter--;\n" +
            "        func = funcAndParams[0];\n" +
            "        params = funcAndParams[1];\n" +
            "        func(params);\n" +
            "    }\n" +
            "  }\n" +
            "})(XMLHttpRequest);\n" +
            "</script>\n";

    @Inject
    private HttpServletRequest httpRequest;

    public String getAjaxCounterScript() {
        String propName = "zanata.countAjax";
        if (Boolean.getBoolean(propName)) {
            return AJAX_COUNTER_SCRIPT;
        }
        return "";
    }

    public String getJavascriptFinishedScript() {
        String propName = "zanata.countAjax";
        String scriptUrl = httpRequest.getContextPath() +
                "/javax.faces.resource/test/finished.js.seam?ln=script";
        if (Boolean.getBoolean(propName)) {
            return "<script defer=\"defer\" type=\"application/javascript\" " +
                    "src=\"" + scriptUrl + "\"></script>";
        }
        return "";
    }
}

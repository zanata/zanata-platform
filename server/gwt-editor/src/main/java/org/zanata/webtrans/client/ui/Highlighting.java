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
package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import org.zanata.util.CoverageIgnore;

public class Highlighting {

    @CoverageIgnore("JSNI")
    public static native void syntaxHighlight(String text, Element elem)
        /*-{
    elem.innerHTML = '';
    $wnd.CodeMirror.runMode(text, "visibleSpace", elem);
    }-*/;

    // From JavaScript on http://www.nsftools.com/misc/SearchAndHighlight.htm
    @CoverageIgnore("JSNI")
    public static native void searchHighlight(String searchTerm, Element elem)
    /*-{
    // the highlightStartTag and highlightEndTag parameters are optional
    var bodyText = elem.innerHTML;

    var highlightStartTag = "<font style='color:blue; background-color:yellow;'>";
    var highlightEndTag = "</font>";

    // find all occurences of the search term in the given text,
    // and add some "highlight" tags to them (we're not using a
    // regular expression search, because we want to filter out
    // matches that occur within HTML tags and script blocks, so
    // we have to do a little extra validation)
    var newText = "";
    var i = -1;
    var lcSearchTerm = searchTerm.toLowerCase();
    var lcBodyText = bodyText.toLowerCase();

    while (bodyText.length > 0) {
      i = lcBodyText.indexOf(lcSearchTerm, i + 1);
      if (i < 0) {
        newText += bodyText;
        bodyText = "";
      } else {
        // skip anything inside an HTML tag
        if (bodyText.lastIndexOf(">", i) >= bodyText.lastIndexOf("<", i)) {
          // skip anything inside a <script> block
          if (lcBodyText.lastIndexOf("/script>", i) >= lcBodyText.lastIndexOf(
              "<script", i)) {
            newText += bodyText.substring(0, i) + highlightStartTag
                + bodyText.substr(i, searchTerm.length) + highlightEndTag;
            bodyText = bodyText.substr(i + searchTerm.length);
            lcBodyText = bodyText.toLowerCase();
            i = -1;
          }
        }
      }
      elem.innerHTML = newText;
    }
    }-*/;

    public static String diffAsHtml(String oldText, String newText) {
        JavaScriptObject diffs = diff(oldText, newText, true);
        return diffsToHtml(diffs);
    }

    @CoverageIgnore("JSNI")
    private static native JavaScriptObject diff(String oldText, String newText,
            boolean cleanupSemantic) /*-{
      return $wnd.JsDiff.diffWords(oldText, newText, {ignoreCase: true});
    }-*/;

    // modified diff_prettyHtml() from diff_match_patch.js
    @CoverageIgnore("JSNI")
    private static native String diffsToHtml(JavaScriptObject diffs)
    /*-{
    var html = [];
    var pattern_amp = /&/g;
    var pattern_lt = /</g;
    var pattern_gt = />/g;
    var pattern_para = /\n/g;
    for ( var x = 0; x < diffs.length; x++) {
      var part = diffs[x];
      var data = diffs[x].value; // Text of change.
      var text = data.replace(pattern_amp, '&amp;').replace(pattern_lt, '&lt;')
          .replace(pattern_gt, '&gt;').replace(pattern_para, '&para;<br>');

      if (part.added) {
        html[x] = '<ins class="diff-insert">' + text + '</ins>';
      } else if (part.removed) {
        html[x] = '<del class="diff-delete">' + text + '</del>';
      } else {
        html[x] = '<span class="diff-equal">' + text + '</span>';
      }
    }
    return html.join('');
    }-*/;

    public static String diffAsHighlight(String oldText, String newText) {
        JavaScriptObject diffs = diff(oldText, newText, false);
        return diffsHighlight(diffs);
    }

    // DIFF_DELETE text is hidden, DIFF_EQUAL text is highlighted, and
    // DIFF_INSERT text is shown plain
    @CoverageIgnore("JSNI")
    private static native String diffsHighlight(JavaScriptObject diffs)
    /*-{
    var html = [];
    var pattern_amp = /&/g;
    var pattern_lt = /</g;
    var pattern_gt = />/g;
    var pattern_para = /\n/g;
    for ( var x = 0; x < diffs.length; x++) {
      var part = diffs[x];
      var data = diffs[x].value; // Text of change.
      var text = data.replace(pattern_amp, '&amp;').replace(pattern_lt, '&lt;')
          .replace(pattern_gt, '&gt;').replace(pattern_para,
              '<span class="newline"></span><br>');

      if (part.added) {
        html[x] = text;
      } else if (part.removed) {
        html[x] = '';
      } else {
        html[x] = '<span class="CodeMirror-searching">' + text + '</span>';
      }
    }
    return html.join('');
    }-*/;

}

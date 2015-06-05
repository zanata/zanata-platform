/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

package org.zanata.seam.text;
import antlr.SemanticException;
import antlr.Token;
import antlr.TokenStream;

/**
 * Converts Seam Text to CommonMark (Markdown).
 *
 * <p>
 * Limitations: In some cases, the CommonMark's HTML rendering will be
 * slightly different from the Seam Text. For example:
 * </p>
 * <ol>
 *     <li>
 *       Seam Text inside HTML tags (eg <code>&lt;p&gt;</code>) or
 *       inside blockquotes (anything surrounded by double quotes
 *       (<code>""</code>) in Seam Text) will be converted to
 *       CommonMark but it will be parsed as plain text by compliant
 *       parsers. Such examples should either be converted to HTML by
 *       hand, or the surrounding tags should be removed.
 *     </li>
 *     <li>
 *       Large blocks of embedded HTML may be interpreted differently
 *       if they include blank lines (thus turning off CommonMark's HTML
 *       block mode).
 *     </li>
 *     <li>
 *       <code>&lt;tt&gt;</code> tags will be replaced by
 *       <code>&lt;code&gt;</code>.
 *     </li>
 *     <li>
 *       <code>&lt;i&gt;</code> tags will be replaced by
 *       <code>&lt;em&gt;</code>.
 *     </li>
 *     <li>
 *       Text surrounded by underscores will be wrapped in
 *       <code>&lt;u&gt;</code> tags for underlining.  Any included Seam
 *       Text will be converted to the equivalent CommonMark but
 *       rendered as plain text.
 *     </li>
 *     <li>Paragraph structure might be slightly different.</li>
 *     <li>
 *       Seam-specific HTML element classes will not be included, such as
 *       <code>&lt;p&gt; class="seamTextPara">&lt;/p&gt;</code> (unless they are
 *       part of HTML tags).
 *     </li>
 * </ol>
 * <p>
 * Sample usage:
 * <blockquote><pre>
 * Reader r = new StringReader(seamtext);
 * SeamTextLexer lexer = new SeamTextLexer(r);
 * SeamTextToCMParser parser = new SeamTextToCMParser(lexer);
 * parser.startRule();
 * return parser.toString()
 * </pre></blockquote></p>
 *
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class SeamTextToCommonMarkParser extends SeamTextParser {

    public SeamTextToCommonMarkParser(TokenStream lexer) {
        super(lexer);
        // we will run a more standard sanitizer separately
        setSanitizer(new DummySanitizer());
    }

    @Override
    protected String backslashEscape(String text) {
        return "\\" + super.backslashEscape(text);
    }

    @Override
    protected String emphasisCloseTag() {
        return "*";
    }

    @Override
    protected String emphasisOpenTag() {
        return "*";
    }

    private String ensureBlankLine() {
        String s = mainBuilder.toString();
        if (s.endsWith("\n\n")) {
            return "";
        } else if (s.endsWith("\n")) {
            return "\n";
        } else {
            return "\n\n";
        }
    }

    @Override
    protected String headline1(String line) {
        return "# " + line;
    }

    @Override
    protected String headline2(String line) {
        return "## " + line;
    }

    @Override
    protected String headline3(String line) {
        return "### " + line;
    }

    @Override
    protected String headline4(String line) {
        return "#### " + line;
    }

    @Override
    protected String linkTag(String description, String url) {
        return "[" + description + "](" + url + ")";
    }

    @Override
    protected String monospaceCloseTag() {
        return "`";
    }

    // NB for strict accuracy, we should probably un-escape HTML entities in the Seam Text while inside a monospace tag
    @Override
    protected String monospaceOpenTag() {
        return "`";
    }

    @Override
    protected String newline(String text) {
        return super.newline(text);
    }

    @Override
    protected String openTagBegin(Token name) {
        return super.openTagBegin(name);
    }

    @Override
    protected String orderedListCloseTag() {
        return "";
    }

    @Override
    protected String orderedListItemCloseTag() {
        return "";
    }

    @Override
    protected String orderedListItemOpenTag() {
        return "1. ";
    }

    @Override
    protected String orderedListOpenTag() {
        return "";
    }

    @Override
    protected String paragraphCloseTag() {
        return "";
    }

    @Override
    protected String paragraphOpenTag() {
        // If the last output was a blank line, we are ready
        // for a new paragraph.
        return ensureBlankLine();
    }

    @Override
    protected String preformattedText(String text) {
        return "\n<pre><code>\n" + text + "\n</code></pre>\n";
    }

    @Override
    protected String quotedCloseTag() {
        return "\"";
    }

    @Override
    protected String quotedOpenTag() {
        return "\"";
    }

    @Override
    protected String underlineCloseTag() {
        return super.underlineCloseTag();
    }

    @Override
    protected String underlineOpenTag() {
        return super.underlineOpenTag();
    }

    @Override
    protected String unorderedListCloseTag() {
        return "";
    }

    @Override
    protected String unorderedListOpenTag() {
        return "";
    }

    @Override
    protected String unorderedListItemCloseTag() {
        return "";
    }

    @Override
    protected String unorderedListItemOpenTag() {
        return "* ";
    }

    class DummySanitizer implements Sanitizer {
        @Override
        public void validateLinkTagURI(Token element, String uri)
                throws SemanticException {
        }

        @Override
        public void validateHtmlElement(Token element)
                throws SemanticException {
        }

        @Override
        public void validateHtmlAttribute(Token element, Token attribute)
                throws SemanticException {
        }

        @Override
        public void validateHtmlAttributeValue(Token element,
                Token attribute,
                String attributeValue) throws SemanticException {
        }

        @Override
        public String getInvalidURIMessage(String uri) {
            return null;
        }

        @Override
        public String getInvalidElementMessage(String elementName) {
            return null;
        }

        @Override
        public String getInvalidAttributeMessage(String elementName,
                String attributeName) {
            return null;
        }

        @Override
        public String getInvalidAttributeValueMessage(String elementName,
                String attributeName, String value) {
            return null;
        }
    }
}

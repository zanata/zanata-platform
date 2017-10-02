// Seam Text implementation copied and adapted from Seam 2.3.1
// (package org.jboss.seam.text).

// $ANTLR 2.7.6 (2005-12-22): "seam-text.g" -> "SeamTextParser.java"$

package org.zanata.seam.text;

import java.util.HashSet;
import java.util.Set;

import antlr.NoViableAltException;
import antlr.ParserSharedInputState;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.Token;
import antlr.TokenBuffer;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.impl.BitSet;

public class SeamTextParser extends antlr.LLkParser implements
        SeamTextParserTokenTypes {

    public static class Macro {
        public String name;
        public java.util.SortedMap<String, String> params =
                new java.util.TreeMap<String, String>();

        public Macro(String name) {
            this.name = name;
        }
    }

    public static class HtmlRecognitionException extends RecognitionException {
        private static final long serialVersionUID = 54632504271583185L;
        Token openingElement;
        RecognitionException wrappedException;

        public HtmlRecognitionException(Token openingElement,
                RecognitionException wrappedException) {
            this.openingElement = openingElement;
            this.wrappedException = wrappedException;
        }

        public Token getOpeningElement() {
            return openingElement;
        }

        public String getMessage() {
            return wrappedException.getMessage();
        }

        public Throwable getCause() {
            return wrappedException;
        }
    }

    /**
     * Sanitization of user input, used to clean links and plain HTML.
     */
    public interface Sanitizer {

        /**
         * Called by the SeamTextParser when a link tag is parsed, i.e. [=>some
         * URI].
         *
         * @param element
         *            the token of the parse tree, here the ">" symbol which
         *            comes after the "="
         * @param uri
         *            the user-entered link text
         * @throws antlr.SemanticException
         *             thrown if the URI is not syntactically or semantically
         *             valid
         */
        public void validateLinkTagURI(Token element, String uri)
                throws SemanticException;

        /**
         * Called by the SeamTextParser when a plain HTML element is parsed.
         *
         * @param element
         *            the token of the parse tree, call <tt>getText()</tt> to
         *            access the HTML tag name
         * @throws antlr.SemanticException
         *             thrown when the HTML tag is not valid
         */
        public void validateHtmlElement(Token element) throws SemanticException;

        /**
         * Called by the SeamTextParser when a plain HTML attribute is parsed.
         *
         * @param element
         *            the token of the parse tree that represents the HTML tag
         * @param attribute
         *            the token of the parse tree that represents the HTML
         *            attribute
         * @throws antlr.SemanticException
         *             thrown if the attribute is not valid for the given HTML
         *             tag
         */
        public void validateHtmlAttribute(Token element, Token attribute)
                throws SemanticException;

        /**
         * Called by the SeamTextParser when a plain HTML attribute value is
         * parsed.
         *
         * @param element
         *            the token of the parse tree that represents the HTML tag
         * @param attribute
         *            the token of the parse tree that represents the HTML
         *            attribute
         * @param attributeValue
         *            the plain string value of the HTML attribute
         * @throws antlr.SemanticException
         *             thrown if the attribute value is not valid for the given
         *             HTML attribute and element
         */
        public void validateHtmlAttributeValue(Token element, Token attribute,
                String attributeValue) throws SemanticException;

        public String getInvalidURIMessage(String uri);

        public String getInvalidElementMessage(String elementName);

        public String getInvalidAttributeMessage(String elementName,
                String attributeName);

        public String getInvalidAttributeValueMessage(String elementName,
                String attributeName, String value);
    }

    /**
     * Implementation of the rules in
     * http://wiki.whatwg.org/wiki/Sanitization_rules
     *
     * <pre>
     * Changes and additions:
     *
     * 1. Expanded all -* wildcard values to their full CSS property name (e.g. border-*).
     *
     * 2. Added dash as allowed characater to REGEX_VALID_CSS_STRING1.
     *
     * 3. Improved REGEX_VALID_CSS_VALUE with range {n,m} checks for color values and negative units.
     *
     * 4. Added more options (mostly of vertical-align property, e.g. "middle", "text-top") as allowed CSS values.
     *
     * 5. Added "max-height", "max-width", "min-height", "min-width" to CSS properties.
     *
     * 6. Removed 'data' URI scheme.
     *
     * 7. Not implemented filtering of CSS url() - it's an invalid value always.
     *
     * 8. Removed all &lt;form&gt;, &lt;input&gt; and other form tags. Attackers might use them to compromise
     *    "outer" forms when entering such markup in a textarea.
     * </pre>
     *
     */
    public static class DefaultSanitizer implements SeamTextParser.Sanitizer {

        public final java.util.regex.Pattern REGEX_VALID_CSS_STRING1 =
                java.util.regex.Pattern
                        .compile(
                        "^([-:,;#%.\\sa-zA-Z0-9!]|\\w-\\w|'[\\s\\w]+'|\"[\\s\\w]+\"|\\([\\d,\\s]+\\))*$"
                        );

        public final java.util.regex.Pattern REGEX_VALID_CSS_STRING2 =
                java.util.regex.Pattern.compile(
                        "^(\\s*[-\\w]+\\s*:\\s*[^:;]*(;|$))*$"
                        );

        public final java.util.regex.Pattern REGEX_VALID_CSS_VALUE =
                java.util.regex.Pattern
                        .compile(
                        "^(#[0-9a-f]{3,6}|rgb\\(\\d{1,3}%?,\\d{1,3}%?,?\\d{1,3}%?\\)?|-?\\d{0,2}\\.?\\d{0,2}(cm|em|ex|in|mm|pc|pt|px|%|,|\\))?)$"
                        );

        public final java.util.regex.Pattern REGEX_INVALID_CSS_URL =
                java.util.regex.Pattern.compile(
                        "url\\s*\\(\\s*[^\\s)]+?\\s*\\)\\s*"
                        );

        protected Set<String> acceptableElements =
                new HashSet<>(java.util.Arrays.asList(
                        "a", "abbr", "acronym", "address", "area", "b", "bdo",
                        "big", "blockquote",
                        "br", "caption", "center", "cite", "code", "col",
                        "colgroup", "dd",
                        "del", "dfn", "dir", "div", "dl", "dt", "em", "font",
                        "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img",
                        "ins", "kbd",
                        "label", "legend", "li", "map", "menu", "ol", "p",
                        "pre", "q", "s", "samp", "small", "span", "strike",
                        "strong",
                        "sub", "sup", "table", "tbody", "td", "tfoot", "th",
                        "thead",
                        "tr", "tt", "u", "ul", "var", "wbr"
                        ));

        protected Set<String> mathmlElements = new HashSet<>(
                java.util.Arrays.asList(
                        "maction", "math", "merror", "mfrac", "mi",
                        "mmultiscripts", "mn", "mo",
                        "mover", "mpadded", "mphantom", "mprescripts", "mroot",
                        "mrow", "mspace",
                        "msqrt", "mstyle", "msub", "msubsup", "msup", "mtable",
                        "mtd", "mtext",
                        "mtr", "munder", "munderover", "none"
                        ));

        protected Set<String> svgElements = new HashSet<>(
                java.util.Arrays.asList(
                        "a", "animate", "animateColor", "animateMotion",
                        "animateTransform",
                        "circle", "defs", "desc", "ellipse", "font-face",
                        "font-face-name",
                        "font-face-src", "g", "glyph", "hkern", "image",
                        "line", "linearGradient",
                        "marker", "metadata", "missing-glyph", "mpath", "path",
                        "polygon",
                        "polyline", "radialGradient", "rect", "set", "stop",
                        "svg", "switch", "text",
                        "title", "tspan", "use"
                        ));

        protected Set<String> acceptableAttributes =
                new HashSet<>(java.util.Arrays.asList(
                        "abbr", "accept", "accept-charset", "accesskey",
                        "action", "align", "alt",
                        "axis", "border", "cellpadding", "cellspacing", "char",
                        "charoff", "charset",
                        "checked", "cite", "class", "clear", "color", "cols",
                        "colspan", "compact",
                        "coords", "datetime", "dir", "disabled", "enctype",
                        "for", "frame",
                        "headers", "height", "href", "hreflang", "hspace",
                        "id", "ismap", "label",
                        "lang", "longdesc", "maxlength", "media", "method",
                        "multiple", "name",
                        "nohref", "noshade", "nowrap", "prompt", "readonly",
                        "rel", "rev", "rows",
                        "rowspan", "rules", "scope", "selected", "shape",
                        "size", "span", "src",
                        "start", "style", "summary", "tabindex", "target",
                        "title", "type", "usemap",
                        "valign", "value", "vspace", "width", "xml:lang"
                        ));

        protected Set<String> mathmlAttributes =
                new HashSet<>(java.util.Arrays.asList(
                        "actiontype", "align", "columnalign", "columnalign",
                        "columnalign",
                        "columnlines", "columnspacing", "columnspan", "depth",
                        "display",
                        "displaystyle", "equalcolumns", "equalrows", "fence",
                        "fontstyle",
                        "fontweight", "frame", "height", "linethickness",
                        "lspace", "mathbackground",
                        "mathcolor", "mathvariant", "mathvariant", "maxsize",
                        "minsize", "other",
                        "rowalign", "rowalign", "rowalign", "rowlines",
                        "rowspacing", "rowspan",
                        "rspace", "scriptlevel", "selection", "separator",
                        "stretchy", "width",
                        "width", "xlink:href", "xlink:show", "xlink:type",
                        "xmlns", "xmlns:xlink"
                        ));

        protected Set<String> svgAttributes = new HashSet<>(
                java.util.Arrays.asList(
                        "accent-height", "accumulate", "additive",
                        "alphabetic", "arabic-form",
                        "ascent", "attributeName", "attributeType",
                        "baseProfile", "bbox", "begin",
                        "by", "calcMode", "cap-height", "class", "color",
                        "color-rendering",
                        "content", "cx", "cy", "d", "descent", "display",
                        "dur", "dx", "dy", "end",
                        "fill", "fill-rule", "font-family", "font-size",
                        "font-stretch",
                        "font-style", "font-variant", "font-weight", "from",
                        "fx", "fy", "g1", "g2",
                        "glyph-name", "gradientUnits", "hanging", "height",
                        "horiz-adv-x",
                        "horiz-origin-x", "id", "ideographic", "k",
                        "keyPoints", "keySplines",
                        "keyTimes", "lang", "marker-end", "marker-mid",
                        "marker-start",
                        "markerHeight", "markerUnits", "markerWidth",
                        "mathematical", "max", "min",
                        "name", "offset", "opacity", "orient", "origin",
                        "overline-position",
                        "overline-thickness", "panose-1", "path", "pathLength",
                        "points",
                        "preserveAspectRatio", "r", "refX", "refY",
                        "repeatCount", "repeatDur",
                        "requiredExtensions", "requiredFeatures", "restart",
                        "rotate", "rx", "ry",
                        "slope", "stemh", "stemv", "stop-color",
                        "stop-opacity",
                        "strikethrough-position", "strikethrough-thickness",
                        "stroke",
                        "stroke-dasharray", "stroke-dashoffset",
                        "stroke-linecap", "stroke-linejoin",
                        "stroke-miterlimit", "stroke-opacity", "stroke-width",
                        "systemLanguage",
                        "target", "text-anchor", "to", "transform", "type",
                        "u1", "u2",
                        "underline-position", "underline-thickness", "unicode",
                        "unicode-range",
                        "units-per-em", "values", "version", "viewBox",
                        "visibility", "width",
                        "widths", "x", "x-height", "x1", "x2", "xlink:actuate",
                        "xlink:arcrole",
                        "xlink:href", "xlink:role", "xlink:show",
                        "xlink:title", "xlink:type",
                        "xml:base", "xml:lang", "xml:space", "xmlns",
                        "xmlns:xlink", "y", "y1", "y2",
                        "zoomAndPan"
                        ));

        protected Set<String> styleProperties =
                new HashSet<>(java.util.Arrays.asList(
                        "azimuth",
                        "background", "background-attachment",
                        "background-color", "background-image",
                        "background-position", "background-repeat",
                        "border", "border-bottom", "border-bottom-color",
                        "border-bottom-style",
                        "border-bottom-width", "border-collapse",
                        "border-color", "border-left",
                        "border-left-color", "border-left-style",
                        "border-left-width", "border-right",
                        "border-right-color", "border-right-style",
                        "border-right-width", "border-spacing",
                        "border-style", "border-top", "border-top-color",
                        "border-top-style",
                        "border-top-width", "border-width",
                        "clear", "color",
                        "cursor", "direction", "display", "elevation", "float",
                        "font",
                        "font-family", "font-size", "font-style",
                        "font-variant", "font-weight",
                        "height", "letter-spacing", "line-height",
                        "margin", "margin-bottom", "margin-left",
                        "margin-right", "margin-top",
                        "max-height", "max-width", "min-height", "min-width",
                        "overflow",
                        "padding", "padding-bottom", "padding-left",
                        "padding-right", "padding-top",
                        "pause", "pause-after", "pause-before", "pitch",
                        "pitch-range", "richness", "speak", "speak-header",
                        "speak-numeral",
                        "speak-punctuation", "speech-rate", "stress",
                        "text-align",
                        "text-decoration", "text-indent", "unicode-bidi",
                        "vertical-align",
                        "voice-family", "volume", "white-space", "width"
                        ));

        protected Set<String> stylePropertiesValues =
                new HashSet<>(java.util.Arrays.asList(
                        "aqua", "auto", "baseline", "black", "block", "blue",
                        "bold", "both", "bottom", "brown",
                        "center", "collapse", "dashed", "dotted", "fuchsia",
                        "gray", "green",
                        "inherit", "italic", "left", "length", "lime",
                        "maroon", "medium", "middle", "navy", "none", "normal",
                        "nowrap", "olive", "percentage", "pointer", "purple",
                        "red", "right", "silver", "solid", "sub", "super",
                        "teal", "text-bottom", "text-top", "top",
                        "transparent", "underline", "white", "yellow"
                        ));

        protected Set<String> svgStyleProperties =
                new HashSet<>(java.util.Arrays.asList(
                        "fill", "fill-opacity", "fill-rule", "stroke",
                        "stroke-linecap",
                        "stroke-linejoin", "stroke-opacity", "stroke-width"
                        ));

        protected Set<String> attributesWhoseValueIsAURI =
                new HashSet<>(java.util.Arrays.asList(
                        "action", "cite", "href", "longdesc", "src",
                        "xlink:href", "xml:base"
                        ));

        protected Set<String> uriSchemes = new HashSet<>(
                java.util.Arrays.asList(
                        "afs", "aim", "callto", "ed2k", "feed", "ftp",
                        "gopher", "http", "https",
                        "irc", "mailto", "news", "nntp", "rsync", "rtsp",
                        "sftp", "ssh", "tag",
                        "tel", "telnet", "urn", "webcal", "wtai", "xmpp"
                        ));

        public void validateLinkTagURI(Token element, String uri)
                throws SemanticException {
            if (!validateURI(uri)) {
                throw createSemanticException("Invalid URI", element);
            }
        }

        public void validateHtmlElement(Token element) throws SemanticException {
            String elementName = element.getText().toLowerCase();

            if (!acceptableElements.contains(elementName) &&
                    !svgElements.contains(elementName) &&
                    !mathmlElements.contains(elementName)) {
                throw createSemanticException(
                        getInvalidElementMessage(elementName), element);
            }
        }

        public void validateHtmlAttribute(Token element, Token attribute)
                throws SemanticException {
            String elementName = element.getText().toLowerCase();
            String attributeName = attribute.getText().toLowerCase();
            if (!acceptableAttributes.contains(attributeName) &&
                    !svgAttributes.contains(attributeName) &&
                    !mathmlAttributes.contains(attributeName)) {
                throw createSemanticException(
                        getInvalidAttributeMessage(elementName, attributeName),
                        element);
            }
        }

        public void validateHtmlAttributeValue(Token element,
                Token attribute,
                String attributeValue) throws SemanticException {

            if (attributeValue == null || attributeValue.length() == 0)
                return;

            String elementName = element.getText().toLowerCase();
            String attributeName = attribute.getText().toLowerCase();

            // Check element with attribute that has URI value (href, src, etc.)
            if (attributesWhoseValueIsAURI.contains(attributeName)
                    && !validateURI(attributeValue)) {
                throw createSemanticException(
                        getInvalidURIMessage(attributeValue), element);
            }

            // Check attribute value of style (CSS filtering)
            if (attributeName.equals("style")) {
                if (!REGEX_VALID_CSS_STRING1.matcher(attributeValue).matches()
                        ||
                        !REGEX_VALID_CSS_STRING2.matcher(attributeValue)
                                .matches()) {
                    throw createSemanticException(
                            getInvalidAttributeValueMessage(elementName,
                                    attributeName, attributeValue),
                            element);
                }

                String[] cssProperties = attributeValue.split(";");
                for (String cssProperty : cssProperties) {
                    if (!cssProperty.contains(":")) {
                        throw createSemanticException(
                                getInvalidAttributeValueMessage(elementName,
                                        attributeName, attributeValue),
                                element);
                    }
                    String[] property = cssProperty.split(":");
                    String propertyName = property[0].trim();
                    String propertyValue =
                            property.length == 2 ? property[1].trim() : null;

                    // CSS property name
                    if (!styleProperties.contains(propertyName) &&
                            !svgStyleProperties.contains(propertyName)) {
                        throw createSemanticException(
                                getInvalidAttributeValueMessage(elementName,
                                        attributeName, attributeValue),
                                element);
                    }

                    // CSS property value
                    if (propertyValue != null
                            && !stylePropertiesValues.contains(propertyValue)) {
                        // Not in list, now check the regex
                        if (!REGEX_VALID_CSS_VALUE.matcher(propertyValue)
                                .matches()) {
                            throw createSemanticException(
                                    getInvalidAttributeValueMessage(
                                            elementName, attributeName,
                                            attributeValue),
                                    element);
                        }
                    }
                }
            }

            // TODO: Implement SVG style checking?! Who cares...
        }

        /**
         * Validate a URI string.
         * <p>
         * The default implementation accepts any URI string that starts with a
         * slash, this is considered a relative URL. Any absolute URI is parsed
         * by the JDK with the <tt>java.net.URI</tt> constructor. Finally, the
         * scheme of the parsed absolute URI is checked with a list of valid
         * schemes.
         * </p>
         *
         * @param uri
         *            the URI string
         * @return return true if the String represents a safe and valid URI
         */
        protected boolean validateURI(String uri) {

            // Relative URI starts with a slash
            if (uri.startsWith("/"))
                return true;

            java.net.URI parsedURI;
            try {
                parsedURI = new java.net.URI(uri);
            } catch (java.net.URISyntaxException ex) {
                return false;
            }

            if (!uriSchemes.contains(parsedURI.getScheme())) {
                return false;
            }
            return true;
        }

        public String getInvalidURIMessage(String uri) {
            return "invalid URI";
        }

        public String getInvalidElementMessage(String elementName) {
            return "invalid element '" + elementName + "'";
        }

        public String getInvalidAttributeMessage(String elementName,
                String attributeName) {
            return "invalid attribute '" + attributeName + "' for element '"
                    + elementName + "'";
        }

        public String getInvalidAttributeValueMessage(String elementName,
                String attributeName, String value) {
            return "invalid value of attribute '" + attributeName
                    + "' for element '" + elementName + "'";
        };

        public SemanticException createSemanticException(String message,
                Token element) {
            return new SemanticException(
                    message,
                    element.getFilename(), element.getLine(),
                    element.getColumn());
        }

    }

    private Sanitizer sanitizer = new DefaultSanitizer();

    public void setSanitizer(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    private Macro currentMacro;
    private java.util.Stack<Token> htmlElementStack =
            new java.util.Stack<Token>();

    protected StringBuilder mainBuilder = new StringBuilder();
    private StringBuilder builder = mainBuilder;

    public String toString() {
        return builder.toString();
    }

    private void append(String... strings) {
        for (String string : strings)
            builder.append(string);
    }

    private void beginCapture() {
        builder = new StringBuilder();
    }

    private String endCapture() {
        String result = builder.toString();
        builder = mainBuilder;
        return result;
    }

    protected String linkTag(String description, String url) {
        return "<a href=\"" + url + "\" class=\"seamTextLink\">" + description
                + "</a>";
    }

    protected String macroInclude(String macroName) {
        return "";
    }

    protected String macroInclude(Macro m) {
        return macroInclude(m.name);
    }

    protected String paragraphOpenTag() {
        return "<p class=\"seamTextPara\">\n";
    }

    protected String preformattedText(String text) {
        return "<pre class=\"seamTextPreformatted\">\n" + text + "</pre>\n";
    }

    protected String blockquoteOpenTag() {
        return "<blockquote class=\"seamTextBlockquote\">\n";
    }

    protected String headline1(String line) {
        return "<h1 class=\"seamTextHeadline1\">" + line + "</h1>";
    }

    protected String headline2(String line) {
        return "<h2 class=\"seamTextHeadline2\">" + line + "</h2>";
    }

    protected String headline3(String line) {
        return "<h3 class=\"seamTextHeadline3\">" + line + "</h3>";
    }

    protected String headline4(String line) {
        return "<h4 class=\"seamTextHeadline4\">" + line + "</h4>";
    }

    protected String orderedListOpenTag() {
        return "<ol class=\"seamTextOrderedList\">\n";
    }

    protected String orderedListItemOpenTag() {
        return "<li class=\"seamTextOrderedListItem\">";
    }

    protected String unorderedListOpenTag() {
        return "<ul class=\"seamTextUnorderedList\">\n";
    }

    protected String unorderedListItemOpenTag() {
        return "<li class=\"seamTextUnorderedListItem\">";
    }

    protected String emphasisOpenTag() {
        return "<i class=\"seamTextEmphasis\">";
    }

    protected String emphasisCloseTag() {
        return "</i>";
    }

    protected SeamTextParser(TokenBuffer tokenBuf, int k) {
        super(tokenBuf, k);
        tokenNames = _tokenNames;
    }

    public SeamTextParser(TokenBuffer tokenBuf) {
        this(tokenBuf, 4);
    }

    protected SeamTextParser(TokenStream lexer, int k) {
        super(lexer, k);
        tokenNames = _tokenNames;
    }

    public SeamTextParser(TokenStream lexer) {
        this(lexer, 4);
    }

    public SeamTextParser(ParserSharedInputState state) {
        super(state, 4);
        tokenNames = _tokenNames;
    }

    public final void startRule() throws RecognitionException,
            TokenStreamException {

        {
            _loop3: do {
                if ((LA(1) == NEWLINE)) {
                    newline();
                } else {
                    break _loop3;
                }

            } while (true);
        }
        {
            switch (LA(1)) {
            case DOUBLEQUOTE:
            case BACKTICK:
            case ALPHANUMERICWORD:
            case UNICODEWORD:
            case PUNCTUATION:
            case SINGLEQUOTE:
            case SLASH:
            case ESCAPE:
            case STAR:
            case BAR:
            case HAT:
            case PLUS:
            case EQ:
            case HASH:
            case TWIDDLE:
            case UNDERSCORE:
            case OPEN:
            case LT:
            case SPACE: {
                {
                    switch (LA(1)) {
                    case PLUS: {
                        heading();
                        {
                            _loop7: do {
                                if ((LA(1) == NEWLINE)) {
                                    newline();
                                } else {
                                    break _loop7;
                                }

                            } while (true);
                        }
                        break;
                    }
                    case DOUBLEQUOTE:
                    case BACKTICK:
                    case ALPHANUMERICWORD:
                    case UNICODEWORD:
                    case PUNCTUATION:
                    case SINGLEQUOTE:
                    case SLASH:
                    case ESCAPE:
                    case STAR:
                    case BAR:
                    case HAT:
                    case EQ:
                    case HASH:
                    case TWIDDLE:
                    case UNDERSCORE:
                    case OPEN:
                    case LT:
                    case SPACE: {
                        break;
                    }
                    default: {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                    }
                }
                text();
                {
                    _loop11: do {
                        if ((LA(1) == PLUS)) {
                            heading();
                            {
                                _loop10: do {
                                    if ((LA(1) == NEWLINE)) {
                                        newline();
                                    } else {
                                        break _loop10;
                                    }

                                } while (true);
                            }
                            text();
                        } else {
                            break _loop11;
                        }

                    } while (true);
                }
                break;
            }
            case EOF: {
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
            }
        }
    }

    public final void newline() throws RecognitionException,
            TokenStreamException {

        Token n = null;

        n = LT(1);
        match(NEWLINE);
        append(newline(n.getText()));
    }

    protected String newline(String text) {
        return text;
    }

    public final void heading() throws RecognitionException,
            TokenStreamException {

        {
            if ((LA(1) == PLUS) && (_tokenSet_0.member(LA(2)))) {
                h1();
            } else if ((LA(1) == PLUS) && (LA(2) == PLUS)
                    && (_tokenSet_0.member(LA(3)))) {
                h2();
            } else if ((LA(1) == PLUS) && (LA(2) == PLUS) && (LA(3) == PLUS)
                    && (_tokenSet_0.member(LA(4)))) {
                h3();
            } else if ((LA(1) == PLUS) && (LA(2) == PLUS) && (LA(3) == PLUS)
                    && (LA(4) == PLUS)) {
                h4();
            } else {
                throw new NoViableAltException(LT(1), getFilename());
            }

        }
        newlineOrEof();
    }

    public final void text() throws RecognitionException, TokenStreamException {

        {
            int _cnt17 = 0;
            _loop17: do {
                if ((_tokenSet_1.member(LA(1)))) {
                    {
                        switch (LA(1)) {
                        case ALPHANUMERICWORD:
                        case UNICODEWORD:
                        case PUNCTUATION:
                        case SINGLEQUOTE:
                        case SLASH:
                        case ESCAPE:
                        case STAR:
                        case BAR:
                        case HAT:
                        case TWIDDLE:
                        case UNDERSCORE:
                        case OPEN:
                        case SPACE: {
                            paragraph();
                            break;
                        }
                        case BACKTICK: {
                            preformatted();
                            break;
                        }
                        case DOUBLEQUOTE: {
                            blockquote();
                            break;
                        }
                        case EQ:
                        case HASH: {
                            list();
                            break;
                        }
                        case LT: {
                            html();
                            break;
                        }
                        default: {
                            throw new NoViableAltException(LT(1), getFilename());
                        }
                        }
                    }
                    {
                        _loop16: do {
                            if ((LA(1) == NEWLINE)) {
                                newline();
                            } else {
                                break _loop16;
                            }

                        } while (true);
                    }
                } else {
                    if (_cnt17 >= 1) {
                        break _loop17;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt17++;
            } while (true);
        }
    }

    public void paragraph() throws RecognitionException, TokenStreamException {

        append(paragraphOpenTag());
        {
            int _cnt20 = 0;
            _loop20: do {
                if ((_tokenSet_0.member(LA(1)))) {
                    line();
                    newlineOrEof();
                } else {
                    if (_cnt20 >= 1) {
                        break _loop20;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt20++;
            } while (true);
        }
        append(paragraphCloseTag());
        newlineOrEof();
    }

    protected String paragraphCloseTag() {
        return "</p>\n";
    }

    public final void preformatted() throws RecognitionException,
            TokenStreamException {

        match(BACKTICK);
        beginCapture();
        {
            _loop30: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD: {
                    word();
                    break;
                }
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH: {
                    punctuation();
                    break;
                }
                case ESCAPE:
                case STAR:
                case BAR:
                case HAT:
                case PLUS:
                case EQ:
                case HASH:
                case TWIDDLE:
                case UNDERSCORE: {
                    specialChars();
                    break;
                }
                case OPEN:
                case CLOSE: {
                    moreSpecialChars();
                    break;
                }
                case DOUBLEQUOTE:
                case GT:
                case LT:
                case AMPERSAND: {
                    htmlSpecialChars();
                    break;
                }
                case SPACE: {
                    space();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    break _loop30;
                }
                }
            } while (true);
        }
        String text = endCapture();
        append(preformattedText(text));
        match(BACKTICK);
    }

    public final void blockquote() throws RecognitionException,
            TokenStreamException {

        match(DOUBLEQUOTE);
        append(blockquoteOpenTag());
        {
            _loop27: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR:
                case BAR:
                case HAT:
                case TWIDDLE:
                case UNDERSCORE: {
                    formatted();
                    break;
                }
                case BACKTICK: {
                    preformatted();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                case LT: {
                    html();
                    break;
                }
                case EQ:
                case HASH: {
                    list();
                    break;
                }
                default: {
                    break _loop27;
                }
                }
            } while (true);
        }
        match(DOUBLEQUOTE);
        newlineOrEof();
        append(blockquoteCloseTag());
    }

    protected String blockquoteCloseTag() {
        return "</blockquote>\n";
    }

    public final void list() throws RecognitionException, TokenStreamException {

        {
            switch (LA(1)) {
            case HASH: {
                olist();
                break;
            }
            case EQ: {
                ulist();
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
            }
        }
        newlineOrEof();
    }

    public final void html() throws RecognitionException, TokenStreamException {

        openTag();
        {
            _loop90: do {
                if ((LA(1) == SPACE)
                        && (LA(2) == SLASH || LA(2) == GT || LA(2) == SPACE)) {
                    space();
                } else if ((LA(1) == SPACE) && (LA(2) == ALPHANUMERICWORD)) {
                    space();
                    attribute();
                } else {
                    break _loop90;
                }

            } while (true);
        }
        {
            switch (LA(1)) {
            case GT: {
                {
                    beforeBody();
                    body();
                    closeTagWithBody();
                }
                break;
            }
            case SLASH: {
                closeTagWithNoBody();
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
            }
        }
    }

    public final void line() throws RecognitionException, TokenStreamException {

        {
            switch (LA(1)) {
            case ALPHANUMERICWORD:
            case UNICODEWORD:
            case PUNCTUATION:
            case SINGLEQUOTE:
            case SLASH:
            case ESCAPE:
            case OPEN:
            case SPACE: {
                plain();
                break;
            }
            case STAR:
            case BAR:
            case HAT:
            case TWIDDLE:
            case UNDERSCORE: {
                formatted();
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
            }
        }
        {
            _loop24: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR:
                case BAR:
                case HAT:
                case TWIDDLE:
                case UNDERSCORE: {
                    formatted();
                    break;
                }
                case BACKTICK: {
                    preformatted();
                    break;
                }
                case DOUBLEQUOTE: {
                    quoted();
                    break;
                }
                case LT: {
                    html();
                    break;
                }
                default: {
                    break _loop24;
                }
                }
            } while (true);
        }
    }

    public final void newlineOrEof() throws RecognitionException,
            TokenStreamException {

        switch (LA(1)) {
        case NEWLINE: {
            newline();
            break;
        }
        case EOF: {
            match(Token.EOF_TYPE);
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    public final void plain() throws RecognitionException, TokenStreamException {

        switch (LA(1)) {
        case ALPHANUMERICWORD:
        case UNICODEWORD: {
            word();
            break;
        }
        case PUNCTUATION:
        case SINGLEQUOTE:
        case SLASH: {
            punctuation();
            break;
        }
        case ESCAPE: {
            escape();
            break;
        }
        case SPACE: {
            space();
            break;
        }
        default:
            if ((LA(1) == OPEN) && (_tokenSet_2.member(LA(2)))) {
                link();
            } else if ((LA(1) == OPEN) && (LA(2) == LT)) {
                macro();
            } else {
                throw new NoViableAltException(LT(1), getFilename());
            }
        }
    }

    public final void formatted() throws RecognitionException,
            TokenStreamException {

        switch (LA(1)) {
        case UNDERSCORE: {
            underline();
            break;
        }
        case STAR: {
            emphasis();
            break;
        }
        case BAR: {
            monospace();
            break;
        }
        case HAT: {
            superscript();
            break;
        }
        case TWIDDLE: {
            deleted();
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    public final void quoted() throws RecognitionException,
            TokenStreamException {

        match(DOUBLEQUOTE);
        append(quotedOpenTag());
        {
            int _cnt68 = 0;
            _loop68: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR: {
                    emphasis();
                    break;
                }
                case UNDERSCORE: {
                    underline();
                    break;
                }
                case BAR: {
                    monospace();
                    break;
                }
                case HAT: {
                    superscript();
                    break;
                }
                case TWIDDLE: {
                    deleted();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    if (_cnt68 >= 1) {
                        break _loop68;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }
                }
                _cnt68++;
            } while (true);
        }
        match(DOUBLEQUOTE);
        append(quotedCloseTag());
    }

    protected String quotedOpenTag() {
        return "<q>";
    }

    protected String quotedCloseTag() {
        return "</q>";
    }

    public final void word() throws RecognitionException, TokenStreamException {

        Token an = null;
        Token uc = null;

        switch (LA(1)) {
        case ALPHANUMERICWORD: {
            an = LT(1);
            match(ALPHANUMERICWORD);
            append(an.getText());
            break;
        }
        case UNICODEWORD: {
            uc = LT(1);
            match(UNICODEWORD);
            append(uc.getText());
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    public final void punctuation() throws RecognitionException,
            TokenStreamException {

        Token p = null;
        Token sq = null;
        Token s = null;

        switch (LA(1)) {
        case PUNCTUATION: {
            p = LT(1);
            match(PUNCTUATION);
            append(p.getText());
            break;
        }
        case SINGLEQUOTE: {
            sq = LT(1);
            match(SINGLEQUOTE);
            append(sq.getText());
            break;
        }
        case SLASH: {
            s = LT(1);
            match(SLASH);
            append(s.getText());
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    public final void specialChars() throws RecognitionException,
            TokenStreamException {

        Token st = null;
        Token b = null;
        Token h = null;
        Token p = null;
        Token eq = null;
        Token hh = null;
        Token e = null;
        Token t = null;
        Token u = null;

        switch (LA(1)) {
        case STAR: {
            st = LT(1);
            match(STAR);
            append(st.getText());
            break;
        }
        case BAR: {
            b = LT(1);
            match(BAR);
            append(b.getText());
            break;
        }
        case HAT: {
            h = LT(1);
            match(HAT);
            append(h.getText());
            break;
        }
        case PLUS: {
            p = LT(1);
            match(PLUS);
            append(p.getText());
            break;
        }
        case EQ: {
            eq = LT(1);
            match(EQ);
            append(eq.getText());
            break;
        }
        case HASH: {
            hh = LT(1);
            match(HASH);
            append(hh.getText());
            break;
        }
        case ESCAPE: {
            e = LT(1);
            match(ESCAPE);
            append(backslashEscape(e.getText()));
            break;
        }
        case TWIDDLE: {
            t = LT(1);
            match(TWIDDLE);
            append(t.getText());
            break;
        }
        case UNDERSCORE: {
            u = LT(1);
            match(UNDERSCORE);
            append(u.getText());
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    protected String backslashEscape(String text) {
        return text;
    }

    public final void moreSpecialChars() throws RecognitionException,
            TokenStreamException {

        Token o = null;
        Token c = null;

        switch (LA(1)) {
        case OPEN: {
            o = LT(1);
            match(OPEN);
            append(o.getText());
            break;
        }
        case CLOSE: {
            c = LT(1);
            match(CLOSE);
            append(c.getText());
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    public final void htmlSpecialChars() throws RecognitionException,
            TokenStreamException {

        switch (LA(1)) {
        case GT: {
            match(GT);
            append("&gt;");
            break;
        }
        case LT: {
            match(LT);
            append("&lt;");
            break;
        }
        case DOUBLEQUOTE: {
            match(DOUBLEQUOTE);
            append("&quot;");
            break;
        }
        case AMPERSAND: {
            match(AMPERSAND);
            append("&amp;");
            break;
        }
        default: {
            throw new NoViableAltException(LT(1), getFilename());
        }
        }
    }

    public final void space() throws RecognitionException, TokenStreamException {

        Token s = null;

        s = LT(1);
        match(SPACE);
        append(s.getText());
    }

    public final void escape() throws RecognitionException,
            TokenStreamException {

        Token b = null;

        match(ESCAPE);
        {
            switch (LA(1)) {
            case ESCAPE:
            case STAR:
            case BAR:
            case HAT:
            case PLUS:
            case EQ:
            case HASH:
            case TWIDDLE:
            case UNDERSCORE: {
                specialChars();
                break;
            }
            case OPEN:
            case CLOSE: {
                moreSpecialChars();
                break;
            }
            case QUOTE: {
                evenMoreSpecialChars();
                break;
            }
            case DOUBLEQUOTE:
            case GT:
            case LT:
            case AMPERSAND: {
                htmlSpecialChars();
                break;
            }
            case BACKTICK: {
                b = LT(1);
                match(BACKTICK);
                append(b.getText());
                break;
            }
            default: {
                throw new NoViableAltException(LT(1), getFilename());
            }
            }
        }
    }

    public final void link() throws RecognitionException, TokenStreamException {

        Token gt = null;

        match(OPEN);
        beginCapture();
        {
            _loop43: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD: {
                    word();
                    break;
                }
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH: {
                    punctuation();
                    break;
                }
                case ESCAPE: {
                    escape();
                    break;
                }
                case SPACE: {
                    space();
                    break;
                }
                default: {
                    break _loop43;
                }
                }
            } while (true);
        }
        String text = endCapture();
        match(EQ);
        gt = LT(1);
        match(GT);
        beginCapture();
        attributeValue();

        String link = endCapture();
        sanitizer.validateLinkTagURI(gt, link);
        append(linkTag(text, link));

        match(CLOSE);
    }

    public final void macro() throws RecognitionException, TokenStreamException {

        Token mn = null;

        match(OPEN);
        match(LT);
        match(EQ);
        mn = LT(1);
        match(ALPHANUMERICWORD);
        currentMacro = new Macro(mn.getText());
        {
            _loop46: do {
                if ((LA(1) == OPEN)) {
                    macroParam();
                } else {
                    break _loop46;
                }

            } while (true);
        }
        match(CLOSE);
        append(macroInclude(currentMacro));
        currentMacro = null;
    }

    public final void underline() throws RecognitionException,
            TokenStreamException {

        match(UNDERSCORE);
        append(underlineOpenTag());
        {
            int _cnt56 = 0;
            _loop56: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR: {
                    emphasis();
                    break;
                }
                case BAR: {
                    monospace();
                    break;
                }
                case HAT: {
                    superscript();
                    break;
                }
                case TWIDDLE: {
                    deleted();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    if (_cnt56 >= 1) {
                        break _loop56;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }
                }
                _cnt56++;
            } while (true);
        }
        match(UNDERSCORE);
        append(underlineCloseTag());
    }

    protected String underlineOpenTag() {
        return "<u>";
    }

    protected String underlineCloseTag() {
        return "</u>";
    }

    public final void emphasis() throws RecognitionException,
            TokenStreamException {

        match(STAR);
        append(emphasisOpenTag());
        {
            int _cnt53 = 0;
            _loop53: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case UNDERSCORE: {
                    underline();
                    break;
                }
                case BAR: {
                    monospace();
                    break;
                }
                case HAT: {
                    superscript();
                    break;
                }
                case TWIDDLE: {
                    deleted();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    if (_cnt53 >= 1) {
                        break _loop53;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }
                }
                _cnt53++;
            } while (true);
        }
        match(STAR);
        append(emphasisCloseTag());
    }

    public final void monospace() throws RecognitionException,
            TokenStreamException {

        Token st = null;
        Token h = null;
        Token p = null;
        Token eq = null;
        Token hh = null;
        Token e = null;
        Token t = null;
        Token u = null;

        match(BAR);
        append(monospaceOpenTag());
        {
            int _cnt59 = 0;
            _loop59: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD: {
                    word();
                    break;
                }
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH: {
                    punctuation();
                    break;
                }
                case SPACE: {
                    space();
                    break;
                }
                case STAR: {
                    st = LT(1);
                    match(STAR);
                    append(st.getText());
                    break;
                }
                case HAT: {
                    h = LT(1);
                    match(HAT);
                    append(h.getText());
                    break;
                }
                case PLUS: {
                    p = LT(1);
                    match(PLUS);
                    append(p.getText());
                    break;
                }
                case EQ: {
                    eq = LT(1);
                    match(EQ);
                    append(eq.getText());
                    break;
                }
                case HASH: {
                    hh = LT(1);
                    match(HASH);
                    append(hh.getText());
                    break;
                }
                case ESCAPE: {
                    e = LT(1);
                    match(ESCAPE);
                    append(e.getText());
                    break;
                }
                case TWIDDLE: {
                    t = LT(1);
                    match(TWIDDLE);
                    append(t.getText());
                    break;
                }
                case UNDERSCORE: {
                    u = LT(1);
                    match(UNDERSCORE);
                    append(u.getText());
                    break;
                }
                case OPEN:
                case CLOSE: {
                    moreSpecialChars();
                    break;
                }
                case DOUBLEQUOTE:
                case GT:
                case LT:
                case AMPERSAND: {
                    htmlSpecialChars();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    if (_cnt59 >= 1) {
                        break _loop59;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }
                }
                _cnt59++;
            } while (true);
        }
        match(BAR);
        append(monospaceCloseTag());
    }

    protected String monospaceOpenTag() {
        return "<tt>";
    }

    protected String monospaceCloseTag() {
        return "</tt>";
    }

    public final void superscript() throws RecognitionException,
            TokenStreamException {

        match(HAT);
        append(superscriptOpenTag());
        {
            int _cnt62 = 0;
            _loop62: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR: {
                    emphasis();
                    break;
                }
                case UNDERSCORE: {
                    underline();
                    break;
                }
                case BAR: {
                    monospace();
                    break;
                }
                case TWIDDLE: {
                    deleted();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    if (_cnt62 >= 1) {
                        break _loop62;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }
                }
                _cnt62++;
            } while (true);
        }
        match(HAT);
        append(superscriptCloseTag());
    }

    protected String superscriptOpenTag() {
        return "<sup>";
    }

    protected String superscriptCloseTag() {
        return "</sup>";
    }

    public final void deleted() throws RecognitionException,
            TokenStreamException {

        match(TWIDDLE);
        append(deletedOpenTag());
        {
            int _cnt65 = 0;
            _loop65: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR: {
                    emphasis();
                    break;
                }
                case UNDERSCORE: {
                    underline();
                    break;
                }
                case BAR: {
                    monospace();
                    break;
                }
                case HAT: {
                    superscript();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default: {
                    if (_cnt65 >= 1) {
                        break _loop65;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }
                }
                _cnt65++;
            } while (true);
        }
        match(TWIDDLE);
        append(deletedCloseTag());
    }

    protected String deletedOpenTag() {
        return "<del>";
    }

    protected String deletedCloseTag() {
        return "</del>";
    }

    public final void evenMoreSpecialChars() throws RecognitionException,
            TokenStreamException {

        Token q = null;

        q = LT(1);
        match(QUOTE);
        append(q.getText());
    }

    public final void attributeValue() throws RecognitionException,
            TokenStreamException {

        Token an = null;
        Token p = null;
        Token s = null;

        try { // for error handling
            {
                _loop107: do {
                    switch (LA(1)) {
                    case AMPERSAND: {
                        match(AMPERSAND);
                        append("&amp;");
                        break;
                    }
                    case ALPHANUMERICWORD: {
                        an = LT(1);
                        match(ALPHANUMERICWORD);
                        append(an.getText());
                        break;
                    }
                    case PUNCTUATION: {
                        p = LT(1);
                        match(PUNCTUATION);
                        append(p.getText());
                        break;
                    }
                    case SLASH: {
                        s = LT(1);
                        match(SLASH);
                        append(s.getText());
                        break;
                    }
                    case SPACE: {
                        space();
                        break;
                    }
                    case ESCAPE:
                    case STAR:
                    case BAR:
                    case HAT:
                    case PLUS:
                    case EQ:
                    case HASH:
                    case TWIDDLE:
                    case UNDERSCORE: {
                        specialChars();
                        break;
                    }
                    default: {
                        break _loop107;
                    }
                    }
                } while (true);
            }
        } catch (RecognitionException ex) {

            // We'd like to have an error reported that names the opening HTML,
            // this
            // helps users to find the actual start of their problem in the wiki
            // text.
            if (htmlElementStack.isEmpty())
                throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }

        }
    }

    public final void macroParam() throws RecognitionException,
            TokenStreamException {

        Token pn = null;

        match(OPEN);
        pn = LT(1);
        match(ALPHANUMERICWORD);
        match(EQ);
        beginCapture();
        macroParamValue();
        String pv = endCapture();
        currentMacro.params.put(pn.getText(), pv);
        match(CLOSE);
    }

    public final void macroParamValue() throws RecognitionException,
            TokenStreamException {

        Token amp = null;
        Token dq = null;
        Token sq = null;
        Token an = null;
        Token p = null;
        Token s = null;
        Token lt = null;
        Token gt = null;

        {
            _loop50: do {
                switch (LA(1)) {
                case AMPERSAND: {
                    amp = LT(1);
                    match(AMPERSAND);
                    append(amp.getText());
                    break;
                }
                case DOUBLEQUOTE: {
                    dq = LT(1);
                    match(DOUBLEQUOTE);
                    append(dq.getText());
                    break;
                }
                case SINGLEQUOTE: {
                    sq = LT(1);
                    match(SINGLEQUOTE);
                    append(sq.getText());
                    break;
                }
                case ALPHANUMERICWORD: {
                    an = LT(1);
                    match(ALPHANUMERICWORD);
                    append(an.getText());
                    break;
                }
                case PUNCTUATION: {
                    p = LT(1);
                    match(PUNCTUATION);
                    append(p.getText());
                    break;
                }
                case SLASH: {
                    s = LT(1);
                    match(SLASH);
                    append(s.getText());
                    break;
                }
                case LT: {
                    lt = LT(1);
                    match(LT);
                    append(lt.getText());
                    break;
                }
                case GT: {
                    gt = LT(1);
                    match(GT);
                    append(gt.getText());
                    break;
                }
                case SPACE: {
                    space();
                    break;
                }
                case ESCAPE:
                case STAR:
                case BAR:
                case HAT:
                case PLUS:
                case EQ:
                case HASH:
                case TWIDDLE:
                case UNDERSCORE: {
                    specialChars();
                    break;
                }
                default: {
                    break _loop50;
                }
                }
            } while (true);
        }
    }

    public final void h1() throws RecognitionException, TokenStreamException {

        match(PLUS);
        beginCapture();
        line();
        String headline = endCapture();
        append(headline1(headline.trim()));
    }

    public final void h2() throws RecognitionException, TokenStreamException {

        match(PLUS);
        match(PLUS);
        beginCapture();
        line();
        String headline = endCapture();
        append(headline2(headline.trim()));
    }

    public final void h3() throws RecognitionException, TokenStreamException {

        match(PLUS);
        match(PLUS);
        match(PLUS);
        beginCapture();
        line();
        String headline = endCapture();
        append(headline3(headline.trim()));
    }

    public final void h4() throws RecognitionException, TokenStreamException {

        match(PLUS);
        match(PLUS);
        match(PLUS);
        match(PLUS);
        beginCapture();
        line();
        String headline = endCapture();
        append(headline4(headline.trim()));
    }

    public final void olist() throws RecognitionException, TokenStreamException {

        append(orderedListOpenTag());
        {
            int _cnt79 = 0;
            _loop79: do {
                if ((LA(1) == HASH)) {
                    olistLine();
                    newlineOrEof();
                } else {
                    if (_cnt79 >= 1) {
                        break _loop79;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt79++;
            } while (true);
        }
        append(orderedListCloseTag());
    }

    protected String orderedListCloseTag() {
        return "</ol>\n";
    }

    public final void ulist() throws RecognitionException, TokenStreamException {

        append(unorderedListOpenTag());
        {
            int _cnt83 = 0;
            _loop83: do {
                if ((LA(1) == EQ)) {
                    ulistLine();
                    newlineOrEof();
                } else {
                    if (_cnt83 >= 1) {
                        break _loop83;
                    } else {
                        throw new NoViableAltException(LT(1), getFilename());
                    }
                }

                _cnt83++;
            } while (true);
        }
        append(unorderedListCloseTag());
    }

    protected String unorderedListCloseTag() {
        return "</ul>\n";
    }

    public final void olistLine() throws RecognitionException,
            TokenStreamException {

        match(HASH);
        append(orderedListItemOpenTag());
        line();
        append(orderedListItemCloseTag());
    }

    protected String orderedListItemCloseTag() {
        return "</li>";
    }

    protected String unorderedListItemCloseTag() {
        return "</li>";
    }

    public final void ulistLine() throws RecognitionException,
            TokenStreamException {

        match(EQ);
        append(unorderedListItemOpenTag());
        line();
        append(unorderedListItemCloseTag());
    }

    public final void openTag() throws RecognitionException,
            TokenStreamException {

        Token name = null;

        try { // for error handling
            match(LT);
            name = LT(1);
            match(ALPHANUMERICWORD);

            htmlElementStack.push(name);
            sanitizer.validateHtmlElement(name);
            append(openTagBegin(name));

        } catch (RecognitionException ex) {

            // We'd like to have an error reported that names the opening HTML,
            // this
            // helps users to find the actual start of their problem in the wiki
            // text.
            if (htmlElementStack.isEmpty())
                throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }

        }
    }

    protected String openTagBegin(Token name) {
        return "<" + name.getText();
    }

    public final void attribute() throws RecognitionException,
            TokenStreamException {

        Token att = null;

        try { // for error handling
            att = LT(1);
            match(ALPHANUMERICWORD);
            {
                _loop102: do {
                    if ((LA(1) == SPACE)) {
                        space();
                    } else {
                        break _loop102;
                    }

                } while (true);
            }
            match(EQ);
            {
                _loop104: do {
                    if ((LA(1) == SPACE)) {
                        space();
                    } else {
                        break _loop104;
                    }

                } while (true);
            }
            match(DOUBLEQUOTE);

            sanitizer.validateHtmlAttribute(htmlElementStack.peek(), att);
            append(att.getText());
            append("=\"");

            beginCapture();

            attributeValue();

            String attValue = endCapture();
            sanitizer.validateHtmlAttributeValue(htmlElementStack.peek(), att,
                    attValue);
            append(attValue);

            match(DOUBLEQUOTE);
            append("\"");
        } catch (RecognitionException ex) {

            // We'd like to have an error reported that names the opening HTML,
            // this
            // helps users to find the actual start of their problem in the wiki
            // text.
            if (htmlElementStack.isEmpty())
                throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }

        }
    }

    public final void beforeBody() throws RecognitionException,
            TokenStreamException {

        try { // for error handling
            match(GT);
            append(openTagEnd());
        } catch (RecognitionException ex) {

            // We'd like to have an error reported that names the opening HTML,
            // this
            // helps users to find the actual start of their problem in the wiki
            // text.
            if (htmlElementStack.isEmpty())
                throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }

        }
    }

    protected String openTagEnd() {
        return ">";
    }

    public final void body() throws RecognitionException, TokenStreamException {

        {
            _loop95: do {
                switch (LA(1)) {
                case ALPHANUMERICWORD:
                case UNICODEWORD:
                case PUNCTUATION:
                case SINGLEQUOTE:
                case SLASH:
                case ESCAPE:
                case OPEN:
                case SPACE: {
                    plain();
                    break;
                }
                case STAR:
                case BAR:
                case HAT:
                case TWIDDLE:
                case UNDERSCORE: {
                    formatted();
                    break;
                }
                case BACKTICK: {
                    preformatted();
                    break;
                }
                case DOUBLEQUOTE: {
                    quoted();
                    break;
                }
                case EQ:
                case HASH: {
                    list();
                    break;
                }
                case NEWLINE: {
                    newline();
                    break;
                }
                default:
                    if ((LA(1) == LT) && (LA(2) == ALPHANUMERICWORD)) {
                        html();
                    } else {
                        break _loop95;
                    }
                }
            } while (true);
        }
    }

    public final void closeTagWithBody() throws RecognitionException,
            TokenStreamException {

        Token name = null;

        match(LT);
        match(SLASH);
        name = LT(1);
        match(ALPHANUMERICWORD);
        match(GT);

        append(closeTag(name));
        htmlElementStack.pop();

    }

    protected String closeTag(Token name) {
        return "</" + name.getText() + ">";
    }

    public final void closeTagWithNoBody() throws RecognitionException,
            TokenStreamException {

        match(SLASH);
        match(GT);

        append(closeOpenTag());
        htmlElementStack.pop();

    }

    protected String closeOpenTag() {
        return "/>";
    }

    private static final String[] _tokenNames = {
            "<0>",
            "the end of the text",
            "<2>",
            "NULL_TREE_LOOKAHEAD",
            "a doublequote \\\"",
            "a backtick '`'",
            "letters or digits",
            "letters or digits",
            "a punctuation character",
            "a single quote '",
            "a slash '/'",
            "the escaping blackslash '\\'",
            "a star '*'",
            "a bar or pipe '|'",
            "a caret '^'",
            "a plus '+'",
            "an equals '='",
            "a hash '#'",
            "a tilde '~'",
            "an underscore '_'",
            "an opening square bracket '['",
            "a closing square bracket ']'",
            "QUOTE",
            "a closing angle bracket '>'",
            "an opening angle bracket '<'",
            "an ampersand '&'",
            "a space or tab",
            "a newline"
    };

    private static final long[] mk_tokenSet_0() {
        long[] data = { 68976576L, 0L };
        return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

    private static final long[] mk_tokenSet_1() {
        long[] data = { 85950448L, 0L };
        return data;
    }

    public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());

    private static final long[] mk_tokenSet_2() {
        long[] data = { 67178432L, 0L };
        return data;
    }

    public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());

}

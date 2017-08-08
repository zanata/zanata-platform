// Seam Text implementation copied and adapted from Seam 2.3.1
// (package org.jboss.seam.text).

// $ANTLR 2.7.6 (2005-12-22): "seam-text.g" -> "SeamTextLexer.java"$

package org.zanata.seam.text;

public interface SeamTextParserTokenTypes {
    int EOF = 1;
    int NULL_TREE_LOOKAHEAD = 3;
    int DOUBLEQUOTE = 4;
    int BACKTICK = 5;
    int ALPHANUMERICWORD = 6;
    int UNICODEWORD = 7;
    int PUNCTUATION = 8;
    int SINGLEQUOTE = 9;
    int SLASH = 10;
    int ESCAPE = 11;
    int STAR = 12;
    int BAR = 13;
    int HAT = 14;
    int PLUS = 15;
    int EQ = 16;
    int HASH = 17;
    int TWIDDLE = 18;
    int UNDERSCORE = 19;
    int OPEN = 20;
    int CLOSE = 21;
    int QUOTE = 22;
    int GT = 23;
    int LT = 24;
    int AMPERSAND = 25;
    int SPACE = 26;
    int NEWLINE = 27;
}

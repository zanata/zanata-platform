// Seam Text implementation copied and adapted from Seam 2.3.1
// (package org.jboss.seam.text).

// $ANTLR 2.7.6 (2005-12-22): "seam-text.g" -> "SeamTextLexer.java"$

package org.zanata.seam.text;

import java.io.InputStream;
import java.io.Reader;
import java.util.Hashtable;

import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.InputBuffer;
import antlr.LexerSharedInputState;
import antlr.NoViableAltForCharException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.collections.impl.BitSet;

public class SeamTextLexer extends antlr.CharScanner implements
        SeamTextParserTokenTypes, TokenStream {
    public SeamTextLexer(InputStream in) {
        this(new ByteBuffer(in));
    }

    public SeamTextLexer(Reader in) {
        this(new CharBuffer(in));
    }

    public SeamTextLexer(InputBuffer ib) {
        this(new LexerSharedInputState(ib));
    }

    public SeamTextLexer(LexerSharedInputState state) {
        super(state);
        caseSensitiveLiterals = true;
        setCaseSensitive(true);
        literals = new Hashtable();
    }

    public Token nextToken() throws TokenStreamException {
        Token theRetToken = null;
        tryAgain: for (;;) {
            Token _token = null;
            int _ttype = Token.INVALID_TYPE;
            resetText();
            try { // for char stream error handling
                try { // for lexical error handling
                    switch (LA(1)) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'g':
                    case 'h':
                    case 'i':
                    case 'j':
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    case 's':
                    case 't':
                    case 'u':
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z': {
                        mALPHANUMERICWORD(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '!':
                    case '$':
                    case '%':
                    case '(':
                    case ')':
                    case ',':
                    case '-':
                    case '.':
                    case ':':
                    case ';':
                    case '?':
                    case '@':
                    case '{':
                    case '}': {
                        mPUNCTUATION(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '=': {
                        mEQ(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '+': {
                        mPLUS(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '_': {
                        mUNDERSCORE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '*': {
                        mSTAR(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '/': {
                        mSLASH(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '\\': {
                        mESCAPE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '|': {
                        mBAR(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '`': {
                        mBACKTICK(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '~': {
                        mTWIDDLE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '"': {
                        mDOUBLEQUOTE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '\'': {
                        mSINGLEQUOTE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '[': {
                        mOPEN(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case ']': {
                        mCLOSE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '#': {
                        mHASH(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '^': {
                        mHAT(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '>': {
                        mGT(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '<': {
                        mLT(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '&': {
                        mAMPERSAND(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '\t':
                    case ' ': {
                        mSPACE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '\n':
                    case '\r': {
                        mNEWLINE(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    case '\uffff': {
                        mEOF(true);
                        theRetToken = _returnToken;
                        break;
                    }
                    default:
                        if ((_tokenSet_0.member(LA(1)))) {
                            mUNICODEWORD(true);
                            theRetToken = _returnToken;
                        } else {
                            if (LA(1) == EOF_CHAR) {
                                uponEOF();
                                _returnToken = makeToken(Token.EOF_TYPE);
                            } else {
                                throw new NoViableAltForCharException(
                                        (char) LA(1), getFilename(), getLine(),
                                        getColumn());
                            }
                        }
                    }
                    if (_returnToken == null)
                        continue tryAgain; // found SKIP token
                    _ttype = _returnToken.getType();
                    _ttype = testLiteralsTable(_ttype);
                    _returnToken.setType(_ttype);
                    return _returnToken;
                } catch (RecognitionException e) {
                    throw new TokenStreamRecognitionException(e);
                }
            } catch (CharStreamException cse) {
                if (cse instanceof CharStreamIOException) {
                    throw new TokenStreamIOException(
                            ((CharStreamIOException) cse).io);
                } else {
                    throw new TokenStreamException(cse.getMessage());
                }
            }
        }
    }

    public final void mALPHANUMERICWORD(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = ALPHANUMERICWORD;
        int _saveIndex;

        {
            int _cnt110 = 0;
            _loop110: do {
                switch (LA(1)) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z': {
                    matchRange('a', 'z');
                    break;
                }
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z': {
                    matchRange('A', 'Z');
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    matchRange('0', '9');
                    break;
                }
                default: {
                    if (_cnt110 >= 1) {
                        break _loop110;
                    } else {
                        throw new NoViableAltForCharException((char) LA(1),
                                getFilename(), getLine(), getColumn());
                    }
                }
                }
                _cnt110++;
            } while (true);
        }
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mUNICODEWORD(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = UNICODEWORD;
        int _saveIndex;

        {
            int _cnt113 = 0;
            _loop113: do {
                if (((LA(1) >= '\u00a0' && LA(1) <= '\u00ff'))) {
                    matchRange('\u00a0', '\u00ff');
                } else if (((LA(1) >= '\u0100' && LA(1) <= '\u017f'))) {
                    matchRange('\u0100', '\u017f');
                } else if (((LA(1) >= '\u0180' && LA(1) <= '\u024f'))) {
                    matchRange('\u0180', '\u024f');
                } else if (((LA(1) >= '\u0250' && LA(1) <= '\ufaff'))) {
                    matchRange('\u0250', '\ufaff');
                } else if (((LA(1) >= '\uff00' && LA(1) <= '\uffef'))) {
                    matchRange('\uff00', '\uffef');
                } else {
                    if (_cnt113 >= 1) {
                        break _loop113;
                    } else {
                        throw new NoViableAltForCharException((char) LA(1),
                                getFilename(), getLine(), getColumn());
                    }
                }

                _cnt113++;
            } while (true);
        }
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mPUNCTUATION(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = PUNCTUATION;
        int _saveIndex;

        switch (LA(1)) {
        case '-': {
            match('-');
            break;
        }
        case ';': {
            match(';');
            break;
        }
        case ':': {
            match(':');
            break;
        }
        case '(': {
            match('(');
            break;
        }
        case ')': {
            match(')');
            break;
        }
        case '{': {
            match('{');
            break;
        }
        case '}': {
            match('}');
            break;
        }
        case '?': {
            match('?');
            break;
        }
        case '!': {
            match('!');
            break;
        }
        case '@': {
            match('@');
            break;
        }
        case '%': {
            match('%');
            break;
        }
        case '.': {
            match('.');
            break;
        }
        case ',': {
            match(',');
            break;
        }
        case '$': {
            match('$');
            break;
        }
        default: {
            throw new NoViableAltForCharException((char) LA(1), getFilename(),
                    getLine(), getColumn());
        }
        }
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mEQ(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = EQ;
        int _saveIndex;

        match('=');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mPLUS(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = PLUS;
        int _saveIndex;

        match('+');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mUNDERSCORE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = UNDERSCORE;
        int _saveIndex;

        match('_');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mSTAR(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = STAR;
        int _saveIndex;

        match('*');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mSLASH(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = SLASH;
        int _saveIndex;

        match('/');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mESCAPE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = ESCAPE;
        int _saveIndex;

        match('\\');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mBAR(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = BAR;
        int _saveIndex;

        match('|');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mBACKTICK(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = BACKTICK;
        int _saveIndex;

        match('`');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mTWIDDLE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = TWIDDLE;
        int _saveIndex;

        match('~');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mDOUBLEQUOTE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = DOUBLEQUOTE;
        int _saveIndex;

        match('"');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mSINGLEQUOTE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = SINGLEQUOTE;
        int _saveIndex;

        match('\'');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mOPEN(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = OPEN;
        int _saveIndex;

        match('[');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mCLOSE(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = CLOSE;
        int _saveIndex;

        match(']');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mHASH(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = HASH;
        int _saveIndex;

        match('#');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mHAT(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = HAT;
        int _saveIndex;

        match('^');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mGT(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = GT;
        int _saveIndex;

        match('>');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mLT(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = LT;
        int _saveIndex;

        match('<');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mAMPERSAND(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = AMPERSAND;
        int _saveIndex;

        match('&');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mSPACE(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = SPACE;
        int _saveIndex;

        {
            int _cnt135 = 0;
            _loop135: do {
                switch (LA(1)) {
                case ' ': {
                    match(' ');
                    break;
                }
                case '\t': {
                    match('\t');
                    break;
                }
                default: {
                    if (_cnt135 >= 1) {
                        break _loop135;
                    } else {
                        throw new NoViableAltForCharException((char) LA(1),
                                getFilename(), getLine(), getColumn());
                    }
                }
                }
                _cnt135++;
            } while (true);
        }
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mNEWLINE(boolean _createToken)
            throws RecognitionException, CharStreamException,
            TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = NEWLINE;
        int _saveIndex;

        if ((LA(1) == '\r') && (LA(2) == '\n')) {
            match("\r\n");
        } else if ((LA(1) == '\r') && (true)) {
            match('\r');
        } else if ((LA(1) == '\n')) {
            match('\n');
        } else {
            throw new NoViableAltForCharException((char) LA(1), getFilename(),
                    getLine(), getColumn());
        }

        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    public final void mEOF(boolean _createToken) throws RecognitionException,
            CharStreamException, TokenStreamException {
        int _ttype;
        Token _token = null;
        int _begin = text.length();
        _ttype = Token.EOF_TYPE;
        int _saveIndex;

        match('\uFFFF');
        if (_createToken && _token == null && _ttype != Token.SKIP) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()
                    - _begin));
        }
        _returnToken = _token;
    }

    private static final long[] mk_tokenSet_0() {
        long[] data = new long[4084];
        data[2] = -4294967296L;
        for (int i = 3; i <= 1003; i++) {
            data[i] = -1L;
        }
        for (int i = 1020; i <= 1022; i++) {
            data[i] = -1L;
        }
        data[1023] = 281474976710655L;
        return data;
    }

    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());

}

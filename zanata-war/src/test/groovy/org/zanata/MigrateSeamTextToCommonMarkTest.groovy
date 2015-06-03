/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata

import antlr.ANTLRException
import antlr.RecognitionException
import liquibase.logging.Logger
import liquibase.logging.core.DefaultLogger
import org.apache.commons.lang.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import org.junit.Assert
import org.junit.ComparisonFailure
import org.junit.Test
import org.zanata.liquibase.custom.MigrateSeamTextToCommonMark
import org.zanata.util.CommonMarkRenderer
import org.zanata.seam.text.SeamTextLexer
import org.zanata.seam.text.SeamTextParser

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class MigrateSeamTextToCommonMarkTest {

    private Logger logger = new DefaultLogger()

    private static String stripNonBreakSpace(String text) {
        return text.replace('\u00A0', ' ').replace("&nbsp;", " ")
    }

    private String convertToHtml(String seamText) {
        Reader r = new StringReader(stripNonBreakSpace(seamText))
        SeamTextLexer lexer = new SeamTextLexer(r)
        SeamTextParser parser = new SeamTextParser(lexer)
        parser.startRule()
        return parser.toString()
    }

    private String convertToCommonMark(String seamText, String name) {
        return MigrateSeamTextToCommonMark.convertToCommonMark(seamText, name, logger)
    }

    private CommonMarkRenderer renderer = new CommonMarkRenderer()

    OutputSettings settings = new OutputSettings().prettyPrint(true).indentAmount(2)

    NodeTraversor nodeCleaner = new NodeTraversor(new NodeVisitor() {
        @Override
        public void head(Node node, int depth) {
        }

        @Override
        public void tail(Node node, int depth) {
            if (node instanceof Element) {
                Element e = (Element) node
                // remove class="seamTextPara" etc
                e.removeAttr("class")
                e.removeAttr("id")
            } else if (node instanceof TextNode) {
                if (node.blank) {
                    // trim empty text nodes
                    node.text('')
                } else {
                    def oldText = node.text()
                    // replace nbsp with ordinary space
                    String text = oldText.replace('\u00A0', ' ')
                    // and trim
                    node.text(text.trim())
                }
            }
        }
    })

    /**
     * Replaces <code>tt</code> with <code>code</code>, strips out
     * <code>class</code> and <code>id</code>, trims whitespace and
     * removes blank text nodes.
     * @param body
     */
    void clean(Element body) {
        body.select('tt').tagName('code')
        body.select('i').tagName('em')
        nodeCleaner.traverse(body)
    }

    /**
     * Prettifies the HTML and normalises some of the minor differences
     * between Seam Text's rendering and CommonMark's.
     * @param html
     * @return
     */
    String normalise(String html) {
        // 1. replace Seam's <q> elements with simple quote characters (")
        // 2. replace Seam's (often erroneous) underlining with underscores.
        // 3. replace CM's <pre><code> with <pre> ala Seam
        String someTagsRemoved = html.replaceAll("</?q>", '"').replaceAll("</?u>", '_').replace('<pre><code>', '<pre>').replace('</code></pre>', '</pre>')
        Element body = Jsoup.parse(someTagsRemoved).outputSettings(settings).body()
        clean(body)
        String souped = body.html()
        // CommonMark sometimes puts <p></p> elements around block-level
        // elements like address, which Jsoup converts to an empty para
        // before and after.  They doesn't seem to affect rendering in
        // browsers though, so we ignore them.
        String noEmptyParas = souped.replaceAll('\\s*<p>\\s*</p>', '')
        // Remove paras because Seam sometimes fails to wrap inline elements in <p></p>
        // Convert em to * because CommonMark ignores markdown inside <p> (converter limitation)
        // Convert _* and *_ to ** because converter does not convert strong emphasis to *** (converter limitation)
        String moreCleaning = noEmptyParas.replaceAll('</?p>', '').replaceAll('</?em>', '*').replace('_*', '**').replace('*_', '**')
        String stripped = StringUtils.strip(moreCleaning)
        return stripped
    }

    static Closure eprintln = System.err.&println

    /**
     * Checks that the HTML rendering of the Seam Text is (almost) the same as
     * the HTML rendering of the CommonMark after conversion from Seam Text.
     */
    private void verifyConversion(String name, String seamText) {
        String commonMark = ''
        String seamToHtml
        String commonMarkToHtml
        String prettySeam
        String prettyCM
        try {
            seamToHtml = convertToHtml(seamText)
            commonMark = convertToCommonMark(seamText, name)
            commonMarkToHtml = renderer.renderToHtmlUnsafe(commonMark)

            prettySeam = normalise(seamToHtml)
            prettyCM = normalise(commonMarkToHtml)
            Assert.assertEquals(prettySeam, prettyCM)
        } catch (ComparisonFailure e) {
            eprintln "$name:"
            eprintln "Seam Text: $seamText"
            eprintln "CommonMark: $commonMark"
            eprintln "Seam Text HTML: $seamToHtml"
            eprintln "CommonMark HTML: $commonMarkToHtml"
            eprintln "CommonMark HTML normalised: $prettyCM"
            throw e
        } catch (ANTLRException e) {
            eprintln name + ":"
            eprintln()
            eprintln e.message
            if (e instanceof RecognitionException) {
                eprintln "line=${e.line} col=${e.column}"
                if (e.line >= 0) {
                    seamText.readLines().each(eprintln)
                    eprintln seamText.readLines().get(e.line)
                    eprintln ' ' * e.column + '^'
                    eprintln '-' * e.column + '|'
                } else {
                    seamText.readLines().take(10).each { eprintln it }
                }
            } else {
                seamText.readLines().take(10).each { eprintln it }
            }
            eprintln ''
            eprintln '\n'
            eprintln '\n'
            eprintln '\n'
            throw e
        }
    }

    @Test
    void testCompressedSeamText() {
        String seamText = """
It's easy to make *emphasis*, |monospace|,
~deleted text~, super^scripts^ or _underlines_.

+This is a big heading
You *must* have some text following a heading!

++This is a smaller heading
This is the first paragraph. We can split it across multiple
lines, but we must end it with a blank line.

This is the second paragraph.

An ordered list:

#first item
#second item
#and even the /third/ item

An unordered list:

=an item
=another item

The other guy said:

"Nyeah nyeah-nee
/nyeah/ nyeah!"

But what do you think he means by "nyeah-nee"?

You can write down equations like 2\\*3\\=6 and HTML tags
like \\<body\\> using the escape character: \\\\.

My code doesn't work:

`for (int i=0; i<100; i--)
{
    doSomething();
}`

Any ideas?

This is a |<tag attribute="value"/>| example.

Go to the Seam website at [=>http://jboss.org/schema/seam].

Go to [the Seam website=>http://jboss.org/schema/seam].

You might want to link to <a href="http://jboss.org/schema/seam">something
cool</a>, or even include an image: <img src="/logo.jpg"/>

<table>
    <tr><td>First name:</td><td>Gavin</td></tr>
    <tr><td>Last name:</td><td>King</td></tr>
</table>
"""
        verifyConversion("compressedSeamText", seamText)
    }

    @Test
    void testGeneralSeamText() {
        String seamText = """

It's easy to make *emphasis*, |monospace|,
~deleted text~, super^scripts^ or _underlines_.




+This is a big heading
You *must* have some text following a heading!

++This is a smaller heading
This is the first paragraph. We can split it across multiple
lines, but we must end it with a blank line.

This is the second paragraph.



An ordered list:

#first item
#second item
#and even the /third/ item

An unordered list:

=an item
=another item




The other guy said:

"Nyeah nyeah-nee
/nyeah/ nyeah!"

But what do you think he means by "nyeah-nee"?





You can write down equations like 2\\*3\\=6 and HTML tags
like \\<body\\> using the escape character: \\\\.





My code doesn't work:

`for (int i=0; i<100; i--)
{
    doSomething();
}`

Any ideas?




This is a |<tag attribute="value"/>| example.




Go to the Seam website at [=>http://jboss.org/schema/seam].



Go to [the Seam website=>http://jboss.org/schema/seam].




You might want to link to <a href="http://jboss.org/schema/seam">something
cool</a>, or even include an image: <img src="/logo.jpg"/>


<table>
    <tr><td>First name:</td><td>Gavin</td></tr>
    <tr><td>Last name:</td><td>King</td></tr>
</table>

"""
        verifyConversion("generalSeamText", seamText)
    }

}

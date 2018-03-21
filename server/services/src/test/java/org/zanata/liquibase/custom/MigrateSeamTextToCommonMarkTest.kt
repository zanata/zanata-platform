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
package org.zanata.liquibase.custom

import antlr.ANTLRException
import antlr.RecognitionException
import liquibase.logging.core.DefaultLogger
import org.apache.commons.lang3.StringUtils
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
import org.zanata.util.CommonMarkRenderer
import org.zanata.seam.text.SeamTextLexer
import org.zanata.seam.text.SeamTextParser
import org.zanata.util.WebJars

// non-breaking spaces
private val NBSP_CHAR = "\u00A0"
private val NBSP_ENTITY = "&nbsp;"

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class MigrateSeamTextToCommonMarkTest {

    private val logger = DefaultLogger()

    private fun stripNonBreakSpace(text: String) =
            text.replace(NBSP_CHAR, " ").replace(NBSP_ENTITY, " ")

    private fun convertToHtml(seamText: String): String {
        val strippedSeamText = stripNonBreakSpace(seamText)
        val parser = SeamTextParser(SeamTextLexer(strippedSeamText.reader()))
        parser.startRule()
        return parser.toString()
    }

    private fun convertToCommonMark(seamText: String, name: String) =
            MigrateSeamTextToCommonMark.convertToCommonMark(seamText, name, logger)

    private val cmRenderer = CommonMarkRenderer(WebJars()).apply { postConstruct() }
    private val jsoupSettings = OutputSettings().prettyPrint(true).indentAmount(2)

    private val nodeCleaner = NodeTraversor(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
        }

        override fun tail(node: Node, depth: Int) {
            if (node is Element) {
                // remove class="seamTextPara" etc
                node.removeAttr("class")
                node.removeAttr("id")
            } else if (node is TextNode) {
                if (node.isBlank) {
                    // trim empty text nodes
                    node.text("")
                } else {
                    val oldText = node.text()
                    // replace nbsp with ordinary space
                    val text = oldText.replace(NBSP_CHAR, " ")
                    // and trim
                    node.text(text.trim())
                }
            }
        }
    })

    /**
     * Replaces `tt` with `code`, strips out `class` and `id`, trims
     * whitespace and removes blank text nodes.
     */
    fun clean(body: Element) {
        body.select("tt").tagName("code")
        body.select("i").tagName("em")
        nodeCleaner.traverse(body)
    }

    /**
     * Prettifies the HTML and normalises some of the minor differences
     * between Seam Text's rendering and CommonMark's.
     */
    fun normalise(html: String): String {
        // 1. replace Seam's <q> elements with simple quote characters (")
        // 2. replace Seam's (often erroneous) underlining with underscores.
        // 3. replace CM's <pre><code> with <pre> ala Seam
        val someTagsRemoved = html.replace("</?q>".toRegex(), "\"").replace("</?u>".toRegex(), "_").replace("<pre><code>", "<pre>").replace("</code></pre>", "</pre>")
        val body = Jsoup.parse(someTagsRemoved).outputSettings(jsoupSettings).body()
        clean(body)
        val souped = body.html()
        // CommonMark sometimes puts <p></p> elements around block-level
        // elements like address, which Jsoup converts to an empty para
        // before and after.  They doesn't seem to affect rendering in
        // browsers though, so we ignore them.
        val noEmptyParas = souped.replace("""\s*<p>\s*</p>""".toRegex(), "")
        // Remove paras because Seam sometimes fails to wrap inline elements in <p></p>
        // Convert em to * because CommonMark ignores markdown inside <p> (converter limitation)
        // Convert _* and *_ to ** because converter does not convert strong emphasis to *** (converter limitation)
        val moreCleaning = noEmptyParas.replace("</?p>".toRegex(), "").replace("</?em>".toRegex(), "*").replace("_*", "**").replace("*_", "**")
        val stripped = StringUtils.strip(moreCleaning)
        return stripped
    }

    private fun eprintln() {
        System.err.println()
    }

    private fun eprintln(arg: String?) {
        System.err.println(arg)
    }

    /**
     * Checks that the HTML rendering of the Seam Text is (almost) the same as
     * the HTML rendering of the CommonMark after conversion from Seam Text.
     */
    private fun verifyConversion(name: String, seamText: String, expectedCM: String) {
        var commonMark = ""
        var seamToHtml = ""
        var commonMarkToHtml = ""
        var prettySeamHtml = ""
        var prettyCMHtml = ""
        try {
            commonMark = convertToCommonMark(seamText, name)
            Assert.assertEquals(expectedCM, commonMark)

            seamToHtml = convertToHtml(seamText)
            prettySeamHtml = normalise(seamToHtml)
            commonMarkToHtml = cmRenderer.renderToHtmlUnsafe(commonMark)
            prettyCMHtml = normalise(commonMarkToHtml)
            Assert.assertEquals(prettySeamHtml, prettyCMHtml)
        } catch (e: ComparisonFailure) {
            eprintln("$name:")
            eprintln("Seam Text: $seamText")
            eprintln("CommonMark: $commonMark")
            eprintln("Seam Text HTML: $seamToHtml")
            eprintln("Pretty Seam HTML: $prettySeamHtml")
            eprintln("CommonMark HTML: $commonMarkToHtml")
            eprintln("CommonMark HTML normalised: $prettyCMHtml")
            throw e
        } catch (e: ANTLRException) {
            eprintln(name + ":")
            eprintln()
            eprintln(e.message)
            if (e is RecognitionException) {
                eprintln("line=${e.line} col=${e.column}")
                if (e.line >= 0) {
                    // seamText.reader().forEachLine { eprintln(it) }
                    eprintln(seamText.reader().readLines().get(e.line))
                    eprintln(" ".repeat(e.column) + "^")
                    eprintln("-".repeat(e.column) + "|")
                } else {
                    seamText.reader().readLines().take(10).forEach { eprintln(it) }
                }
            } else {
                seamText.reader().readLines().take(10).forEach { eprintln(it) }
            }
            eprintln()
            eprintln("\n")
            eprintln("\n")
            eprintln("\n")
            throw e
        }
    }

    @Test
    fun `compact Seam Text`() {
        val seamText = """
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

You can write down equations like 2\*3\=6 and HTML tags
like \<body\> using the escape character: \\.

My code doesn't work:

`for (int i=0; i<100; i--)
{
    doSomething();
}`

Any ideas?

Go to the Seam website at [=>http://jboss.org/schema/seam].

Go to [the Seam website=>http://jboss.org/schema/seam].

You might want to link to <a href="http://jboss.org/schema/seam">something
cool</a>, or even include an image: <img src="/logo.jpg"/>

<table>
    <tr><td>First name:</td><td>Gavin</td></tr>
    <tr><td>Last name:</td><td>King</td></tr>
</table>
"""
        val expectedCM =
                """<!-- The following text was converted from Seam Text to CommonMark by Zanata.  Some formatting changes may have occurred. -->



It's easy to make *emphasis*, `monospace`,
<del>deleted text</del>, super<sup>scripts</sup> or <u>underlines</u>.

# This is a big heading

You *must* have some text following a heading!

## This is a smaller heading

This is the first paragraph. We can split it across multiple
lines, but we must end it with a blank line.

This is the second paragraph.

An ordered list:

1. first item
1. second item
1. and even the /third/ item

An unordered list:

* an item
* another item

The other guy said:

<blockquote class="seamTextBlockquote">
Nyeah nyeah-nee
/nyeah/ nyeah!
</blockquote>

But what do you think he means by "nyeah-nee"?

You can write down equations like 2*3=6 and HTML tags
like &lt;body&gt; using the escape character: \\.

My code doesn't work:


<pre><code>
for (int i=0; i&lt;100; i--)
{
    doSomething();
}
</code></pre>


Any ideas?

Go to the Seam website at [](http://jboss.org/schema/seam).

Go to [the Seam website](http://jboss.org/schema/seam).

You might want to link to <a href="http://jboss.org/schema/seam">something
cool</a>, or even include an image: <img src="/logo.jpg"/>

<table>
    <tr><td>First name:</td><td>Gavin</td></tr>
    <tr><td>Last name:</td><td>King</td></tr>
</table>
"""
        verifyConversion("compressedSeamText", seamText, expectedCM)
    }

    @Test
    fun `ordinary Seam Text`() {
        val seamText = """

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





You can write down equations like 2\*3\=6 and HTML tags
like \<body\> using the escape character: \\.





My code doesn't work:

`for (int i=0; i<100; i--)
{
    doSomething();
}`

Any ideas?




Go to the Seam website at [=>http://jboss.org/schema/seam].



Go to [the Seam website=>http://jboss.org/schema/seam].




You might want to link to <a href="http://jboss.org/schema/seam">something
cool</a>, or even include an image: <img src="/logo.jpg"/>


<table>
    <tr><td>First name:</td><td>Gavin</td></tr>
    <tr><td>Last name:</td><td>King</td></tr>
</table>

"""
        val expectedCM =
                """<!-- The following text was converted from Seam Text to CommonMark by Zanata.  Some formatting changes may have occurred. -->



It's easy to make *emphasis*, `monospace`,
<del>deleted text</del>, super<sup>scripts</sup> or <u>underlines</u>.




# This is a big heading

You *must* have some text following a heading!

## This is a smaller heading

This is the first paragraph. We can split it across multiple
lines, but we must end it with a blank line.

This is the second paragraph.



An ordered list:

1. first item
1. second item
1. and even the /third/ item

An unordered list:

* an item
* another item




The other guy said:

<blockquote class="seamTextBlockquote">
Nyeah nyeah-nee
/nyeah/ nyeah!
</blockquote>

But what do you think he means by "nyeah-nee"?





You can write down equations like 2*3=6 and HTML tags
like &lt;body&gt; using the escape character: \\.





My code doesn't work:


<pre><code>
for (int i=0; i&lt;100; i--)
{
    doSomething();
}
</code></pre>


Any ideas?




Go to the Seam website at [](http://jboss.org/schema/seam).



Go to [the Seam website](http://jboss.org/schema/seam).




You might want to link to <a href="http://jboss.org/schema/seam">something
cool</a>, or even include an image: <img src="/logo.jpg"/>


<table>
    <tr><td>First name:</td><td>Gavin</td></tr>
    <tr><td>Last name:</td><td>King</td></tr>
</table>

"""
        verifyConversion("generalSeamText", seamText, expectedCM)
    }

}

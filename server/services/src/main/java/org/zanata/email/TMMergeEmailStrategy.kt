package org.zanata.email

import kotlinx.html.*
import org.zanata.common.ContentState
import org.zanata.common.ContentState.*
import org.zanata.i18n.Messages
import org.zanata.service.tm.merge.TMMergeResult

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

data class ProjectInfo(val name: String, val url: String)
data class VersionInfo(val slug: String, val url: String)
// TODO perhaps extract serverURL to GenericEmailContext
data class MergeContext(val serverURL: String, val addresses: EmailAddressBlock, val project: ProjectInfo, val version: VersionInfo, val matchRange: IntRange)

class TMMergeEmailStrategy(
        private val context: MergeContext,
        private val mergeResult: TMMergeResult): HtmlEmailStrategy() {
    override fun getSubject(msgs: Messages): String =
            msgs["email.templates.tm_merge.Results"]!!
    override fun getReceivedReasons(msgs: Messages): List<String> =
            listOf(msgs["email.templates.tm_merge.TriggeredByYou"]!!)
    override val addresses: EmailAddressBlock = context.addresses
    override fun bodyProducer() =
            tmMergeEmailBodyProducer(context, mergeResult)
}

// inline styles
private object s {
    // based on class styles:
    val branding = """
        color: #03A6D7;
        font-weight: 500;
        letter-spacing: 0.1rem;
        text-transform: uppercase;
        font-size: 3rem;
    """.trimIndent()
    const val noUnderline = "text-decoration: none;"
    const val approved = "color: #20718A;"
    const val translated = "color: #62c876;"
    const val fuzzy = "color: #ffa800;"
    const val container = "width: 80%; margin-left: auto; margin-right: auto;"
    const val dark = "color: #546677;"
    const val light = "color: #A2B3BE; font-weight: 500;"
    const val lowWeight = "font-weight: 300;"
    const val marginLeft0 = "margin-left:0;"
    const val padRight20 = "padding-right: 20px;"

    // based on element styles:
    const val text = "font-family: 'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif;"
    const val h1 = "color:#20718A; font-weight: 400;"
    const val h2 = "margin-bottom: 0;"
    const val h3 = "text-align: left; font-size: 1rem; font-weight: 400; margin-bottom: 0;"
    const val table = "width: 80%;"
    const val inTable = "line-height: 2rem; background-color: #fefefe;"
    const val td = "border-bottom: solid 1px #BDD4DC; color: #555;"
    val ul = """
        padding-left: 0;
        margin: 0;
        display: inline-flex;
        list-style-type:none;
    """.trimIndent()
}

private val ContentState.niceName: String
    get() = when (this) {
        Approved, Translated -> name
        NeedReview -> "Fuzzy"
        else -> name
    }
private val ContentState.style: String
    get() = when (this) {
        Approved -> s.approved
        Translated -> s.translated
        NeedReview -> s.fuzzy
        else -> ""
    }

private fun tmMergeEmailBodyProducer(context: MergeContext, mergeResult: TMMergeResult): BODY.(Messages) -> Unit = { msgs ->
    div {
        style = s.container + s.text
        a(href = context.serverURL) {
            style = s.noUnderline
            img {
//                height = "42px"
                src = "http://zanata.org/assets/logo-sm.png"
            }
            span {
                style = s.branding
                +"Zanata"
            }
        }
        h1 {
            style = s.h1 + s.lowWeight
            +msgs["email.templates.tm_merge.Results"]!!
        }
        ul {
            style = s.ul
            li {
                style = s.dark + s.marginLeft0 + s.padRight20
                +"${msgs["jsf.Project"]}: "
                a(href = context.project.url) {
                    style = s.dark
                    +context.project.name
                }
            }
            li {
                style = s.dark + s.marginLeft0
                +"${msgs["jsf.Version"]}: "
                a(href = context.version.url) {
                    style = s.dark
                    +context.version.slug
                }
            }
        }
        p {
            style = s.dark
            +msgs["email.templates.tm_merge.MatchRange"]!!
            +": "
            span {
                style = s.light
                + msgs.format("email.templates.tm_merge.MatchRangeFromXToY", context.matchRange.first, context.matchRange.last)!!
            }
        }
        // hr is useful for the plain text version generated from this HTML
        hr {
            style = "display:none;"
        }

        // FIXME band states
        for (state in listOf(Approved, Translated, NeedReview)) {
//        for (state in mergeResult.states) {
            h2 {
                style = s.h2 + s.lowWeight + state.style
                +msgs.format("email.templates.tm_merge.CopiedAs", state.niceName)
            }
            // FIXME band ranges
            for (range in listOf(80 until 90, 90 until 100, 100 until 101)) {
                h3 {
                    style = s.h3 +  s.light
                    if (range.first == 100)
                        +msgs["email.templates.tm_merge.100Match"]!!
                    else
                        +msgs.format("email.templates.tm_merge.XToYRangeMatch", range.first, range.last)!!
                }
                table {
                    style = s.table
                    tbody {
                        style = s.inTable
                        tr {
                            style = s.inTable
                            td {
                                style = s.td + s.inTable
                                // FIXME use counters from TM merge result
                                +msgs.format("email.templates.tm_merge.WordsCharsMessages", 67, 200, 12)
                            }
                        }
                    }
                }
            }
            // hr is useful for the plain text version generated from this HTML
            hr {
                style = "display:none;"
            }
        }
    }
}


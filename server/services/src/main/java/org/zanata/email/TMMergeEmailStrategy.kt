package org.zanata.email

import kotlinx.html.*
import org.zanata.common.ContentState
import org.zanata.common.ContentState.*
import org.zanata.i18n.Messages
import org.zanata.service.tm.merge.TMMergeResult
import javax.mail.internet.InternetAddress

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

/**
 * The name and full URL of a project
 */
data class ProjectInfo(val name: String, val url: String)

/**
 * The slug and full URL of a project version
 */
data class VersionInfo(val slug: String, val url: String)

/**
 * This data class represents the context of a TM merge operation, to help build the email.
 * @param toAddresses the addresses which should receive the email
 * @param project the project which is the target of the TM merge
 * @param version the project version which is the target of the TM merge
 * @param matchRange the full set of ranges which were enabled for Fuzzy copies
 * during the TM merge operation (eg 70-100%)
 */
data class TMMergeEmailContext(val toAddresses: List<InternetAddress>, val project: ProjectInfo, val version: VersionInfo, val matchRange: IntRange)

/**
 * The HtmlEmailStrategy for TM Merge results
 */
// It might be better pass context/mergeResult to methods which need them, not to constructor
class TMMergeEmailStrategy(
        val context: TMMergeEmailContext,
        val mergeResult: TMMergeResult): HtmlEmailStrategy() {

    override fun getSubject(msgs: Messages): String =
            msgs["email.templates.tm_merge.Results"]!!
    override fun getReceivedReasons(msgs: Messages): List<String> =
            listOf(msgs["email.templates.tm_merge.TriggeredByYou"]!!)
    override val addresses: EmailAddressBlock = EmailAddressBlock(
            fromAddress = null,
            toAddresses = context.toAddresses)
    override fun bodyProducer(generalContext: GeneralEmailContext) =
            tmMergeEmailBodyProducer(generalContext, context, mergeResult)
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
    const val error = "color: red;"
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

/**
 * Returns a human-readable name for a ContentState, suitable for using in the TM Merge report.
 */
private val ContentState.niceName: String
    get() = when (this) {
        NeedReview -> "Fuzzy"
        else -> name
    }

/**
 * Returns the inline style which should be used for the section heading corresponding to a ContentState
 */
private val ContentState.style: String
    get() = when (this) {
        Approved -> s.approved
        Translated -> s.translated
        NeedReview -> s.fuzzy
        else -> s.error
    }

private fun tmMergeEmailBodyProducer(generalContext: GeneralEmailContext, context: TMMergeEmailContext, mergeResult: TMMergeResult): BODY.(Messages) -> Unit = { msgs ->
    div {
        style = s.container + s.text
        a(href = generalContext.serverURL) {
            style = s.noUnderline
            img {
                src = "http://zanata.org/assets/logo-sm.png"
            }
            span {
                style = s.branding
                +msgs["jsf.Zanata"]!!
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

        for (state in mergeResult.contentStates) {
            if (mergeResult.noMessagesCounted(state)) continue
            h2 {
                style = s.h2 + s.lowWeight + state.style
                +msgs.format("email.templates.tm_merge.CopiedAs", state.niceName)
            }
            for (range in mergeResult.rangesForContentState(state)) {
                if (mergeResult.noMessagesCounted(state, range)) continue
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

                                val ctr = mergeResult.getCounter(state, range)
                                +msgs.format("email.templates.tm_merge.WordsCharsMessages", ctr.words, ctr.codePoints, ctr.messages)
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


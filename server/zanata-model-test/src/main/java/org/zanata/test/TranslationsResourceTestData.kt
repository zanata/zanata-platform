package org.zanata.test

import org.zanata.common.ContentState
import org.zanata.rest.dto.extensions.comment.SimpleComment
import org.zanata.rest.dto.extensions.gettext.HeaderEntry
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.rest.dto.resource.TranslationsResource

object TranslationsResourceTestData {

    @JvmStatic
    val testTranslationsResource: TranslationsResource
        get() {
            return TranslationsResource().apply {
                textFlowTargets.add(TextFlowTarget("rest1").apply {
                    setContents("hello world")
                    state = ContentState.Approved
                    getExtensions(true)
                })
                getExtensions(true)
            }
        }

    @JvmStatic
    val testTranslationsResourceWith2Targets: TranslationsResource
        get() {
            return TranslationsResource().apply {
                val target = TextFlowTarget("rest1").apply {
                    setContents("hello world")
                    state = ContentState.Approved
                    getExtensions(true)
                }
                textFlowTargets.add(target)
                val target2 = TextFlowTarget("rest2").apply {
                    setContents("greeting world")
                    state = ContentState.Approved
                    getExtensions(true)
                }
                textFlowTargets.add(target2)
                getExtensions(true)
            }
        }

    @JvmStatic
    val testTextFlowTargetComment: TranslationsResource
        get() {
            val sr = testTranslationsResource
            val stf = sr.textFlowTargets[0]

            val simpleComment = SimpleComment("textflowtarget comment")

            stf.getExtensions(true).add(simpleComment)
            return sr
        }

    @JvmStatic
    val testPoTargetHeaderTextFlowTarget: TranslationsResource
        get() {
            val sr = testTranslationsResource
            val poTargetHeader = PoTargetHeader(
                    "target header comment\nAdmin user <root@localhost>, 2011. #zanata",
                    HeaderEntry("ht", "vt1"), HeaderEntry("th2",
                    "tv2"), HeaderEntry("Content-Type",
                    "charset=UTF-8"))

            sr.getExtensions(true).add(poTargetHeader)
            return sr
        }

}

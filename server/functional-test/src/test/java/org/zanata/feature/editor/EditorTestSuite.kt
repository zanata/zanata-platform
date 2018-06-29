package org.zanata.feature.editor

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(EditorAlphaTest::class,
        EditorFilterMessagesTest::class,
        TranslateTextTest::class,
        TranslationHistoryTest::class)
class EditorTestSuite

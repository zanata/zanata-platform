package org.zanata.feature.document;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DownloadDocumentTest.class,
    MultiFileUploadTest.class,
    UploadTest.class
})
public class DocumentTestSuite {
}

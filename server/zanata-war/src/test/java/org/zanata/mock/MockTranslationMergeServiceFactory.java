package org.zanata.mock;

import org.zanata.service.TranslationMergeService;
import org.zanata.service.impl.TranslationMergeServiceFactory;

import javax.enterprise.inject.Specializes;

@Specializes
public class MockTranslationMergeServiceFactory extends TranslationMergeServiceFactory {
    @Override
    public TranslationMergeService getMergeService(MergeContext mergeContext) {
        TranslationMergeService mergeService = super.getMergeService(mergeContext);

        return new MockTranslationMergeApproved(mergeService);
    }
}

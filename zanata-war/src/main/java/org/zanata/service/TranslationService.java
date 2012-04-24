/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public interface TranslationService
{
   TranslationResult translate(Long textFlowId, LocaleId localeId, ContentState contentState, List<String> targetContents);

   /**
    * Translates all text flows in a document.
    *
    * @param projectSlug The project to translate
    * @param iterationSlug The project iteration to translate
    * @param docId The document identifier to translate
    * @param locale The locale that the translations belong to
    * @param translations The translations to save to the document
    * @param extensions The extensions to use while translating
    * @param mergeType Indicates how to handle the translations. AUTO will merge the new translations with the provided
    *                  ones. IMPORT will overwrite all existing translations with the new ones.
    * @return A list of unknown resIds that were not found in the document being translated.
    */
   Collection<String> translateAll(String projectSlug, String iterationSlug, String docId, LocaleId locale, TranslationsResource translations, Set<String> extensions,
                                          MergeType mergeType);

   public interface TranslationResult
   {
      HTextFlow getTextFlow();
      HTextFlowTarget getPreviousTextFlowTarget();
      HTextFlowTarget getNewTextFlowTarget();
   }
}

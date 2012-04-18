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

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

public interface TranslationService
{
   TranslationResult translate(Long textFlowId, LocaleId localeId, ContentState contentState, List<String> targetContents);

   public interface TranslationResult
   {
      HTextFlow getTextFlow();
      HTextFlowTarget getPreviousTextFlowTarget();
      HTextFlowTarget getNewTextFlowTarget();
   }
}

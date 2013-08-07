/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service;

import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.zanata.common.LocaleId;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationId;

/**
 * Defines a Cache Service for translation states.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface TranslationStateCache
{
   /**
    * Returns a {@link OpenBitSet} of translated text flows, where the bits
    * represent the Ids of {@link org.zanata.model.HTextFlow} entries that have
    * been translated for the given Locale Id
    * 
    * @param localeId
    * @return An OpenBitSet
    */
   OpenBitSet getTranslatedTextFlowIds(LocaleId localeId);

   /**
    * Returns a Lucene Filter which only returns
    * {@link org.zanata.model.HTextFlow}s which have been translated for the
    * given Locale Id
    * 
    * @param targetLocale
    * @return
    */
   Filter getFilter(LocaleId localeId);

   /**
    * Informs the cache that a text flow has changed its state in a given
    * locale. (It's really a Text Flow Target state)
    * 
    * @param textFlowId The id of the text flow that has changed state.
    * @param localeId The locale for which state has changed.
    * @param newState The new state after the change.
    */
   void textFlowStateUpdated(TextFlowTargetStateEvent event);

   /**
    * Returns DocumentStatus of last modified HTextFlowTarget for the given
    * locale id of the documentId
    * 
    * @param documentId
    * @param localeId
    * @return
    */
   DocumentStatus getDocumentStatus(Long documentId, LocaleId localeId);
	
	/**
    * Return boolean of textFlowTarget has validation error against validation
    * rules {@link org.zanata.webtrans.share.model.ValidationAction}
    * 
    * @param textFlowTargetId
    * @param validationId
    * @return
    */
   Boolean textFlowTargetHasError(Long textFlowTargetId, ValidationId validationId);
}

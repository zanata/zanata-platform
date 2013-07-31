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
package org.zanata.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.sf.ehcache.CacheManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.zanata.cache.CacheWrapper;
import org.zanata.cache.EhcacheWrapper;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationStateCache;
import org.zanata.service.ValidationFactoryProvider;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentStatus;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.cache.CacheLoader;

/**
 * Default Implementation of the Translation State Cache.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("translationStateCacheImpl")
@Scope(ScopeType.APPLICATION)
public class TranslationStateCacheImpl implements TranslationStateCache
{
   private static final String BASE = TranslationStateCacheImpl.class.getName();
   private static final String DOC_STATS_CACHE_NAME = BASE + ".docStatsCache";
   private static final String TFT_VALIDATION_CACHE_NAME = BASE + ".targetValidationCache";

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In
   private ValidationService validationServiceImpl;

   private CacheManager cacheManager;
   private CacheWrapper<TranslatedDocumentKey, DocumentStatus> docStatsCache;
   private CacheWrapper<Long, Map<ValidationId, Boolean>> targetValidationCache;
   private CacheLoader<TranslatedDocumentKey, DocumentStatus> docStatsLoader;
   private CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader;

   public TranslationStateCacheImpl()
   {
      // constructor for Seam
      this.docStatsLoader = new HTextFlowTargetIdLoader();
      this.targetValidationLoader = new HTextFlowTargetValidationLoader();
   }

   public TranslationStateCacheImpl(
         CacheLoader<TranslatedDocumentKey,
         DocumentStatus> docStatsLoader,
         CacheLoader<Long, Map<ValidationId, Boolean>> targetValidationLoader,
         TextFlowTargetDAO textFlowTargetDAO,
         ValidationService validationServiceImpl)
   {
      // constructor for testing
      this.docStatsLoader = docStatsLoader;
      this.targetValidationLoader = targetValidationLoader;
      this.textFlowTargetDAO = textFlowTargetDAO;
      this.validationServiceImpl = validationServiceImpl;
   }

   @Create
   public void create()
   {
      cacheManager = CacheManager.create();
      docStatsCache = EhcacheWrapper.create(DOC_STATS_CACHE_NAME, cacheManager, docStatsLoader);
      targetValidationCache = EhcacheWrapper.create(TFT_VALIDATION_CACHE_NAME, cacheManager, targetValidationLoader);
   }

   @Destroy
   public void destroy()
   {
      cacheManager.shutdown();
   }

   /**
    * This method contains all logic to be run immediately after a Text Flow Target has
    * been successfully translated.
    * <p>
    * Note: it is not safe to access the database, since this event is triggered after the transaction has completed.
    */
   @Observer(TextFlowTargetStateEvent.EVENT_NAME)
   @Override
   public void textFlowStateUpdated(TextFlowTargetStateEvent event)
   {
      // TODO update this cache rather than invalidating
      invalidateDocLastTranslatedCache(event.getDocumentId(), event.getLocaleId());
      invalidateTargetValidationCache(event.getTextFlowTargetId());
   }

   @Override
   public DocumentStatus getDocStats(Long documentId, LocaleId localeId)
   {
      return docStatsCache.getWithLoader(new TranslatedDocumentKey(documentId, localeId));
   }

   @Override
   public Boolean textFlowTargetHasError(Long targetId, ValidationId validationId)
   {
      Map<ValidationId, Boolean> cacheEntry = targetValidationCache.getWithLoader(targetId);
      if (!cacheEntry.containsKey(validationId))
      {
         Boolean result = loadTargetValidation(targetId, validationId);
         cacheEntry.put(validationId, result);
      }
      return cacheEntry.get(validationId);
   }

   private void invalidateDocLastTranslatedCache(Long documentId, LocaleId localeId)
   {
      docStatsCache.remove(new TranslatedDocumentKey(documentId, localeId));
   }

   private void invalidateTargetValidationCache(Long textFlowTargetId)
   {
      targetValidationCache.remove(textFlowTargetId);
   }

   private final class HTextFlowTargetIdLoader extends CacheLoader<TranslatedDocumentKey, DocumentStatus>
   {
      @Override
      public DocumentStatus load(TranslatedDocumentKey key) throws Exception
      {
         HTextFlowTarget target = documentDAO.getLastTranslatedTarget(key.getDocumentId(), key.getLocaleId());
         HDocument document = documentDAO.findById(key.getDocumentId(), false);

         boolean hasError = validationServiceImpl.runDocValidationsWithServerRules(document, key.getLocaleId());

         Date lastTranslatedDate = null;
         String lastTranslatedBy = "";

         if (target != null)
         {
            lastTranslatedDate = target.getLastChanged();

            if (target.getLastModifiedBy() != null)
            {
               lastTranslatedBy = target.getLastModifiedBy().getAccount().getUsername();
            }
         }
         return new DocumentStatus(new DocumentId(document.getId(), document.getDocId()), hasError, lastTranslatedDate, lastTranslatedBy);
      }
   }

   private final class HTextFlowTargetValidationLoader extends CacheLoader<Long, Map<ValidationId, Boolean>>
   {
      @Override
      public Map<ValidationId, Boolean> load(Long key) throws Exception
      {
         return new HashMap<ValidationId, Boolean>();
      }
   }

   private Boolean loadTargetValidation(Long textFlowTargetId, ValidationId validationId)
   {
      HTextFlowTarget tft = textFlowTargetDAO.findById(textFlowTargetId, false);

      if (tft != null)
      {
         ValidationAction action = ValidationFactoryProvider.getFactoryInstance().getValidationAction(validationId);
         List<String> errorList = action.validate(tft.getTextFlow().getContents().get(0), tft.getContents().get(0));
         return !errorList.isEmpty();
      }
      return null;
   }

   @AllArgsConstructor
   @EqualsAndHashCode
   public static final class TranslatedDocumentKey implements Serializable
   {
      private static final long serialVersionUID = 1L;

      @Getter
      private Long documentId;

      @Getter
      private LocaleId localeId;
   }
}

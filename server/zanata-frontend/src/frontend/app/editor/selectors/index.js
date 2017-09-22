/* Selectors are functions that extract specific sections from the redux store.
 * They can select calculated state such as the current page of phrases.
 *
 * Uses:
 *
 *  - in mapStateToProps to get the required state
 *  - with redux-watch to select the part of state to observe
 *
 * Memoization:
 *
 * Selectors will cache their results and only recompute when the input values
 * change. The default comparison for createSelector is ===. You can make a new
 * version of createSelector with:
 *
 *   createSelectorCreator(memoizer, comparer)
 *
 * You can use reselect.defaultMemoize as the memoizer and swap in any
 * comparison function.
 */

import { createSelector, createSelectorCreator, defaultMemoize } from 'reselect'
import { isEqual, isNaN, max } from 'lodash'
import { hasAdvancedFilter } from '../utils/filter-util'

export const getLang = state => state.context.lang
// FIXME move detail to detail[lang] and add timestamps
const getPhrasesDetail = state => state.phrases.detail

const getSelectedPhraseId = state => state.phrases.selectedPhraseId
export const getSelectedPhrase = createSelector(
  getSelectedPhraseId, getPhrasesDetail,
  (phraseId, detail) => detail[phraseId]
)

// TODO move docId elsewhere in state
const getDocId = state => state.context.docId

export const getPageIndex = state => state.phrases.paging.pageIndex
const getCountPerPage = state => state.phrases.paging.countPerPage

const getFilter = state => state.phrases.filter
const getAdvancedFilter = state => state.phrases.filter.advanced
export const getHasAdvancedFilter = createSelector(getAdvancedFilter,
  advancedFilter => hasAdvancedFilter(advancedFilter))

const getPhrasesInDoc = state => state.phrases.inDoc
const getPhrasesInDocFiltered = state => state.phrases.inDocFiltered

/* always returns an array, may be empty */
const getCurrentDocPhrases = createSelector(
  getDocId, getPhrasesInDoc, getPhrasesInDocFiltered, getHasAdvancedFilter,
  (docId, inDoc, inDocFiltered, hasAdvancedFilter) => {
    const phrases = hasAdvancedFilter ? inDocFiltered[docId] : inDoc[docId]
    return phrases || []
  }
)

export const getFilteredPhrases = createSelector(
  getCurrentDocPhrases, getFilter,
  (phrases, { status }) => {
    if (status.all) {
      return phrases
    }
    return phrases.filter(phrase => {
      return status[phrase.status]
    })
  }
)

const getCurrentPagePhrases = createSelector(
  getFilteredPhrases, getPageIndex, getCountPerPage,
  (phrases, pageIndex, countPerPage) => {
    const startIndex = pageIndex * countPerPage
    const stopIndex = startIndex + countPerPage
    return phrases.slice(startIndex, stopIndex)
  }
)

const getLocales = state => state.headerData.context.projectVersion.locales

const getLocale = createSelector(
  getLang, getLocales,
  (lang, locales) => locales[lang]
)

/* Visible phrases that do not have data yet.
 *
 * Must only use this when phrase detail is not requesting, to avoid requesting
 * the same detail twice.
 *
 * TODO add timestamps to detail items to allow including stale phrases here
 */
export const getMissingPhrases = createSelector(
  getCurrentPagePhrases, getPhrasesDetail,
  (currentPage, detail) =>
    currentPage.filter(({ id }) => !detail.hasOwnProperty(id))
)

const getFetchingPhraseDetail = state => state.phrases.fetchingDetail

/* Data needed for fetching phrase detail.
 *
 * Phrases, locale object, and whether details is currently fetching.
 * Locale may be undefined.
 */
export const getPhraseDetailFetchData =
  // deep equal since filtered phrases can be a new array with the same contents
  createSelectorCreator(defaultMemoize, isEqual)(
    getMissingPhrases, getLocale, getFetchingPhraseDetail,
    (phrases, locale, fetching) => ({ phrases, locale, fetching })
  )

/* Detail for the current page, falling back on flyweight. */
export const getCurrentPagePhraseDetail = createSelector(
  getCurrentPagePhrases, getPhrasesDetail,
  (currentPage, detail) => {
    return currentPage.map(flyweight => detail[flyweight.id]
      ? detail[flyweight.id] : flyweight)
  }
)

// may be undefined
export const getLocation = state => state.routing.locationBeforeTransitions
// may be undefined
const getLocationPage = createSelector(
  getLocation, location => location ? location.query.page : undefined)

// page number according to query string, adjusted to be valid
export const getLocationPageNumber = createSelector(getLocationPage,
  (pageString) => {
    const pageNum = parseInt(pageString, 10)
    const pageIndex = pageNum - 1
    return isNaN(pageIndex) ? 0 : max([pageIndex, 0])
  })

// -1 when there are no pages (i.e. no phrases)
export const getMaxPageIndex = createSelector(
  getFilteredPhrases, getCountPerPage,
  (phrases, countPerPage) => {
    const maxPageNumber = Math.ceil(phrases.length / countPerPage)
    // from number to 0-based index
    return maxPageNumber - 1
  }
)

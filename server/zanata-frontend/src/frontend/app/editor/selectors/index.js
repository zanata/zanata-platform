/* Selectors are functions that extract specific sections from the redux store.
 * They can select calculated state such as the current page of phrases.
 *
 * Uses:
 *
 *  - in mapStateToProps to get the required state
 *  - with redux-watch to select the part of state to observe
 */

import { createSelector } from 'reselect'

const getLang = state => state.context.lang
// FIXME move detail to detail[lang] and add timestamps
const getPhrasesDetail = state => state.phrases.detail

const getPageIndex = state => state.phrases.paging.pageIndex
const getCountPerPage = state => state.phrases.paging.countPerPage

const getFilter = state => state.phrases.filter

// TODO move docId elsewhere in state
// TODO can have a selector that decides whether to use plain or server-filtered
const getCurrentDocPhrases = state => state.phrases.inDoc[state.context.docId]

// FIXME same as filter-paging-util filterPhrases(), replace that
const getFilteredPhrases = createSelector(
  getCurrentDocPhrases, getFilter,
  (phrases, { status }) => {
    if (status.all) {
      return phrases
    }
    const filtered = phrases.filter(phrase => {
      return status[phrase.status]
    })
    return filtered
  }
)

// FIXME same as filter-paging-util getCurrentPhrasesFromState(), replace that
const getCurrentPagePhrases = createSelector(
  getFilteredPhrases, getPageIndex, getCountPerPage,
  (phrases, pageIndex, countPerPage) => {
    const startIndex = pageIndex * countPerPage
    const stopIndex = startIndex + countPerPage
    return phrases.slice(startIndex, stopIndex)
  }
)

export const getCurrentPagePhrasesAndLang = createSelector(
  getCurrentPagePhrases, getLang,
  (phrases, lang) => ({ phrases, lang })
)

/* Detail for the current page, falling back on flyweight. */
export const getCurrentPagePhraseDetail = createSelector(
  getCurrentPagePhrases, getPhrasesDetail,
  (currentPage, detail) => {
    return currentPage.map(flyweight => detail[flyweight.id]
      ? detail[flyweight.id] : flyweight)
  }
)

/* Visible phrases that do not have data yet.
 * Careful here - do not want to keep firing requests for phrases that are
 * already fetching.
 *
 * FIXME could include stale phrases here
 * FIXME need state indicating which phrases have been requested recently
 * Note: this will trigger when fetchingPhrases updates, but usually the list
 *       would be empty since all the ids that made it through before will be
 *       filtered out in the first step.
 */
// export const getMissingPhrases = createSelector(
//   getCurrentPagePhrases, getPhrasesDetail,
//   (currentPage, detail) => {
//     // TODO add fetchingPhrases set in state
//     const fetchingPhrases = new Set([])
//     currentPage
//       .filter(phrase => !fetchingPhrases.has(phrase.id))
//       .map(({ id }) => id)
//       .filter(id => !detail.hasOwnProperty(id))
//   }
// )

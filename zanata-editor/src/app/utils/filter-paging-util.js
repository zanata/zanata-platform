/**
 * Utility functions for filtering and paging text flows
 */

export function getCurrentPagePhrasesFromState (state) {
  const { pageIndex, countPerPage } = state.phrases.paging
  const filtered = getFilteredPhrasesFromState(state)
  const startIndex = pageIndex * countPerPage
  const stopIndex = startIndex + countPerPage
  return filtered.slice(startIndex, stopIndex)
}

export function getFilteredPhrasesFromState (state) {
  return filterPhrases(state.ui.textFlowDisplay.filter,
                       getSelectedDocPhrasesFromState(state))
}

/**
 * Given phrase summary list (detail not needed), get
 * phrases that match the given filter.
 */
export function filterPhrases (filter, phrases) {
  if (filter.all) {
    return phrases
  }
  const filtered = phrases.filter(phrase => {
    return filter[phrase.status]
  })
  return filtered
}

export function calculateMaxPageIndexFromState (state) {
  return calculateMaxPageIndex(getFilteredPhrasesFromState(state),
                              state.phrases.paging.countPerPage)
}

// -1 when there are no pages (i.e. no phrases)
export function calculateMaxPageIndex (phrases, countPerPage) {
  const phraseCount = phrases.length

  const maxPageNumber = Math.ceil(phraseCount / countPerPage)
  return maxPageNumber - 1
}

export function getSelectedDocPhrasesFromState (state) {
  // may be nothing if the phrases have not loaded yet
  return state.phrases.inDoc[state.context.docId] || []
}

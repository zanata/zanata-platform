import { getSuggestions } from '../api/suggestions'
import { debounce, isUndefined } from 'lodash'

export const TOGGLE_SUGGESTIONS = Symbol('TOGGLE_SUGGESTIONS')
export function toggleSuggestions () {
  return {
    type: TOGGLE_SUGGESTIONS
  }
}

/**
 * Make phrase search visible or hidden.
 *
 * If the phrase search panel is shown, it will just hide the suggestions
 * panel. If suggestions are hidden or showing text search suggestions, the
 * suggestion panel will be visible and will show phrase suggestions.
 */
export function togglePhraseSuggestions () {
  return (dispatch, getState) => {
    const panelVisible = getState().ui.panels.suggestions.visible
    const phraseSearchVisible =
      panelVisible && getState().suggestions.searchType === 'phrase'

    dispatch(setSuggestionSearchType('phrase'))
    if (phraseSearchVisible || !panelVisible) {
      dispatch(toggleSuggestions())
    }
  }
}

export const DIFF_SETTING_CHANGED = Symbol('DIFF_SETTING_CHANGED')
export function diffSettingChanged () {
  return { type: DIFF_SETTING_CHANGED }
}

export function clearSearch () {
  return changeSearchText('')
}

/**
 * Start a text search when the search text stops changing for a quarter-second.
 *
 * This must not be nested in the action creator function, otherwise each call
 * uses a separate debounce copy and it doesn't actually work.
 */
const dispatchFindTextSuggestionsWhenInactive = debounce(
  (dispatch, searchText) => {
    dispatch(findTextSuggestions(searchText))
  }, 250)

export function changeSearchText (searchText) {
  return (dispatch, getState) => {
    dispatch(suggestionSearchTextChange(searchText))
    dispatchFindTextSuggestionsWhenInactive(dispatch, searchText)
  }
}

export const SET_SUGGESTION_SEARCH_TYPE = Symbol('SET_SUGGESTION_SEARCH_TYPE')
export function setSuggestionSearchType (type) {
  if (type !== 'phrase' && type !== 'text') {
    console.error('invalid search type', type)
  }
  return { type: SET_SUGGESTION_SEARCH_TYPE, searchType: type }
}

export function toggleSearchType () {
  return (dispatch, getState) => {
    const wasTypeText = getState().suggestions.searchType === 'text'
    if (!wasTypeText) {
      dispatch(changeSearchText(''))
    }
    dispatch(setSuggestionSearchType(wasTypeText ? 'phrase' : 'text'))
  }
}

export const RESET_SUGGESTIONS_COPYING = Symbol('RESET_SUGGESTIONS_COPYING')
export function resetSuggestionsCopying () {
  return { type: RESET_SUGGESTIONS_COPYING }
}

export const COPY_SUGGESTION_N = Symbol('COPY_SUGGESTION_N')
export function copySuggestionN (index) {
  // Decision: keep the logic in here to choose what to copy
  //   reason: reducers are not an easy place to follow complex logic,
  //           they should mainly handle merging data

  return (dispatch, getState) => {
    const { searchType } = getState().suggestions
    const { selectedPhraseId } = getState().phrases
    const panelVisible = getState().ui.panels.suggestions.visible

    const isTextSuggestions = panelVisible && searchType === 'text'

    if (isTextSuggestions) {
      dispatch(copyTextSuggestionN(index))
    } else {
      dispatch(copyPhraseSuggestionN(selectedPhraseId, index))
    }
  }
}

function copyTextSuggestionN (index) {
  return (dispatch, getState) => {
    const { suggestions } = getState().suggestions.textSearch
    if (suggestions && index < suggestions.length) {
      dispatch(textSuggestionStartedCopying(index))
      dispatch(copySuggestion(suggestions[index]))
      setTimeout(
        () => dispatch(textSuggestionFinishedCopying(index)),
        500)
    }
  }
}

function copyPhraseSuggestionN (phraseId, index) {
  return (dispatch, getState) => {
    const { searchByPhrase } = getState().suggestions
    const { suggestions } = searchByPhrase[phraseId]
    if (suggestions && index < suggestions.length) {
      dispatch(phraseSuggestionStartedCopying(phraseId, index))
      dispatch(copySuggestion(suggestions[index]))
      setTimeout(
        () => dispatch(phraseSuggestionFinishedCopying(phraseId, index)),
        500)
    }
  }
}

export const COPY_SUGGESTION = Symbol('COPY_SUGGESTION')
function copySuggestion (suggestion) {
  return { type: COPY_SUGGESTION, suggestion }
}

export const TEXT_SUGGESTION_STARTED_COPYING =
  Symbol('TEXT_SUGGESTION_STARTED_COPYING')
function textSuggestionStartedCopying (index) {
  return { type: TEXT_SUGGESTION_STARTED_COPYING, index }
}

export const TEXT_SUGGESTION_FINISHED_COPYING =
  Symbol('TEXT_SUGGESTION_FINISHED_COPYING')
function textSuggestionFinishedCopying (index) {
  return { type: TEXT_SUGGESTION_FINISHED_COPYING, index }
}

export const PHRASE_SUGGESTION_STARTED_COPYING =
  Symbol('PHRASE_SUGGESTION_STARTED_COPYING')
function phraseSuggestionStartedCopying (phraseId, index) {
  return { type: PHRASE_SUGGESTION_STARTED_COPYING, phraseId, index }
}

export const PHRASE_SUGGESTION_FINISHED_COPYING =
  Symbol('PHRASE_SUGGESTION_FINISHED_COPYING')
function phraseSuggestionFinishedCopying (phraseId, index) {
  return { type: PHRASE_SUGGESTION_FINISHED_COPYING, phraseId, index }
}

export const TEXT_SUGGESTIONS_UPDATED = Symbol('TEXT_SUGGESTIONS_UPDATED')
export function textSuggestionsUpdated (
  {loading, searchStrings, suggestions, timestamp}) {
  return {
    type: TEXT_SUGGESTIONS_UPDATED,
    loading,
    searchStrings,
    suggestions,
    timestamp
  }
}

export const SUGGESTION_SEARCH_TEXT_CHANGE =
  Symbol('SUGGESTION_SEARCH_TEXT_CHANGE')
export function suggestionSearchTextChange (text) {
  return { type: SUGGESTION_SEARCH_TEXT_CHANGE, text: text }
}

// TODO may want to throttle as well to prevent generating too many concurrent
//      requests on a slow connection (e.g. 5s latency = 20 requests)
export function findTextSuggestions (searchText) {
  return (dispatch, getState) => {
    // TODO also dispatch search timestamp to state
    const timestamp = Date.now()

    // TODO stop if this is a repeat of the current search
    // TODO use cached search result if there is a recent one
    //      (alternating 'a' and backspace in textbox would only hit server
    //       once until the cached result for 'a' is old enough to be stale)

    // empty search should immediately return no results and no search strings
    if (!searchText) {
      dispatch(textSuggestionsUpdated({
        loading: false,
        searchStrings: [],
        suggestions: [],
        timestamp
      }))
      return
    }

    const searchStrings = [searchText]

    // dispatching this means that any earlier searches will not display their
    // results (because their timestamp is older than the one for the loading
    // search)
    dispatch(textSuggestionsUpdated({
      loading: true,
      searchStrings,
      suggestions: [],
      timestamp
    }))

    const { context } = getState()
    const sourceLocale = context.sourceLocale.localeId
    const transLocale = context.lang
    getSuggestions(sourceLocale, transLocale, searchStrings)
      .then(suggestions => {
        // only dispatch results if there is not a newer searches
        // (but do dispatch when timestamp is the same, as it is an update of
        // the current search progress)
        const currentTimestamp = getState().suggestions.textSearch.timestamp
        if (timestamp >= currentTimestamp) {
          dispatch(textSuggestionsUpdated({
            loading: false,
            searchStrings,
            suggestions,
            timestamp
          }))
        }
        // TODO trigger pending search if it exists
      })
      .catch(error => {
        // TODO report error visible to user
        // TODO set the text search to an error state, which can be displayed
        console.error(error)
      })
  }
}

/**
 * Trigger a phrase search using the detail for the given phrase id.
 *
 * When the detail is not available, this will retry every 0.5 seconds until
 * the detail object is present, and will fail after 20 retries.
 *
 * This is needed mainly during document load because phrase selection happens
 * before the detail is available.
 */
export function findPhraseSuggestionsById (phraseId) {
  return (dispatch, getState) => {
    if (isUndefined(phraseId)) {
      return
    }
    waitForPhraseDetail(20)

    function waitForPhraseDetail (times) {
      const phrase = getState().phrases.detail[phraseId]
      if (phrase) {
        dispatch(findPhraseSuggestions(phrase))
      } else if (times > 0) {
        // FIXME need a better way than polling to trigger this search as soon
        //       as the phrase detail is available.
        setTimeout(() => {
          waitForPhraseDetail(times - 1)
        }, 500)
      } else {
        console.error('No detail available for phrase search after 20 tries. ' +
          'phraseId: ' + phraseId)
      }
    }
  }
}

const PHRASE_SEARCH_STALE_AGE_MILLIS = 60000

export function findPhraseSuggestions (phrase) {
  return (dispatch, getState) => {
    const phraseId = phrase.id
    const searchStrings = [...phrase.sources]
    const timestamp = Date.now()

    // if there are recent results, just leave them as-is and skip this search
    const cachedSearch = getState().suggestions.searchByPhrase[phraseId]
    if (cachedSearch) {
      const age = timestamp - cachedSearch.timestamp
      if (age < PHRASE_SEARCH_STALE_AGE_MILLIS) {
        return
      }
    }

    // set loading state, but only when there are no existing results
    // (stale results are very likely accurate, so leave them as a placeholder)
    if (!cachedSearch) {
      dispatch(phraseSuggestionsUpdated({
        phraseId,
        loading: true,
        searchStrings,
        suggestions: [],
        timestamp
      }))
    }

    const { context } = getState()
    const sourceLocale = context.sourceLocale.localeId
    const transLocale = context.lang
    getSuggestions(sourceLocale, transLocale, searchStrings)
      .then(suggestions => {
        dispatch(phraseSuggestionsUpdated({
          phraseId,
          loading: false,
          searchStrings,
          suggestions,
          timestamp
        }))
      })
      .catch(error => {
        // TODO report error visible to user
        console.error(error)
      })
  }
}

export const PHRASE_SUGGESTIONS_UPDATED = Symbol('PHRASE_SUGGESTIONS_UPDATED')
export function phraseSuggestionsUpdated (
    {phraseId, loading, searchStrings, suggestions, timestamp}) {
  return {
    type: PHRASE_SUGGESTIONS_UPDATED,
    phraseId,
    loading,
    searchStrings,
    suggestions,
    timestamp
  }
}

export const SUGGESTION_PANEL_HEIGHT_CHANGE =
  Symbol('SUGGESTION_PANEL_HEIGHT_CHANGE')
export function saveSuggestionPanelHeight (percentageHeight) {
  return {
    type: SUGGESTION_PANEL_HEIGHT_CHANGE,
    percentageHeight
  }
}

export const SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX =
  Symbol('SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX')

/**
 * Open or close the suggestion detail modal.
 *
 * @param indexOrUndefined undefined to hide the modal, index of suggestion in
 *   the current suggestion list to show detail for that suggestion in the modal
 */
export function showDetailForSuggestionByIndex (indexOrUndefined) {
  return {
    type: SHOW_DETAIL_FOR_SUGGESTION_BY_INDEX,
    index: indexOrUndefined
  }
}

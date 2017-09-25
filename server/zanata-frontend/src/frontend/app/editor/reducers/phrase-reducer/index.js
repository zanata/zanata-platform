import updateObject from 'immutability-helper'
import phraseFilterReducer, { defaultState as defaultFilterState }
  from './phrase-filter-reducer'
import { composeReducers, subReducer } from 'redux-sac'
import {
  CLAMP_PAGE,
  UPDATE_PAGE
} from '../../actions/controls-header-actions'
import { COPY_GLOSSARY_TERM } from '../../actions/glossary-action-types'
import {
  CANCEL_EDIT,
  COPY_FROM_ALIGNED_SOURCE,
  COPY_FROM_SOURCE,
  PHRASE_DETAIL_REQUEST,
  PHRASE_LIST_REQUEST,
  PHRASE_LIST_SUCCESS,
  PHRASE_LIST_FAILURE,
  PENDING_SAVE_INITIATED,
  PHRASE_DETAIL_SUCCESS,
  PHRASE_TEXT_SELECTION_RANGE,
  QUEUE_SAVE,
  SAVE_FINISHED,
  SAVE_INITIATED,
  SELECT_PHRASE,
  SELECT_PHRASE_SPECIFIC_PLURAL,
  TRANSLATION_TEXT_INPUT_CHANGED,
  UNDO_EDIT
} from '../../actions/phrases-action-types'
import { COPY_SUGGESTION } from '../../actions/suggestions-action-types'
import {
  getFilteredPhrases,
  getHasAdvancedFilter,
  getMaxPageIndex
} from '../../selectors'
import { hasAdvancedFilter } from '../../utils/filter-util'
import { replaceRange } from '../../utils/string-utils'
import { SET_SAVE_AS_MODE } from '../../actions/key-shortcuts-actions'
import { MOVE_NEXT, MOVE_PREVIOUS
} from '../../actions/phrase-navigation-actions'
import { findIndex } from 'lodash'

// FIXME this reducer is too big. See if it can be split up.

// TODO use lodash when upgraded
// clamps a number within the inclusive lower and upper bounds
function clamp (number, lower, upper) {
  return Math.max(lower, Math.min(number, upper))
}

const defaultState = {
  fetchingList: false,
  fetchingFilteredList: false,
  filteredListTimestamp: new Date(0),
  fetchingDetail: false,
  saveAsMode: false,
  // expected shape: { [docId1]: [{ id, resId, status }, ...], [docId2]: [...] }
  inDoc: {},
  inDocFiltered: {},

  // expected shape: { [phraseId1]: phrase-object, [phraseId2]: ..., ...}
  detail: {},
  selectedPhraseId: undefined,
  /* Cursor/selection position within the currently editing translation, used
   * for inserting terms from glossary etc. */
  selectedTextRange: {
    start: 0,
    end: 0
  },
  paging: {
    countPerPage: 20,
    pageIndex: 0
  },
  filter: defaultFilterState
}

export const phraseReducer = (state = defaultState, action) => {
  switch (action.type) {
    case CLAMP_PAGE:
      return update({
        paging: {
          pageIndex: {$set: clamp(state.paging.pageIndex, 0,
            getMaxPageIndex(action.getState()))}
        }
      })

    case UPDATE_PAGE:
      return updatePageIndex(action.payload)

    case CANCEL_EDIT:
      // Discard any newTranslations that were entered.
      const currentTrans = state.detail[state.selectedPhraseId].translations
      return update({
        selectedPhraseId: {$set: undefined},
        detail: {
          [state.selectedPhraseId]: {
            newTranslations: {$set: currentTrans}
          }
        }
      })

    case COPY_FROM_ALIGNED_SOURCE:
      return updatePhrase(state.selectedPhraseId, {$apply: (phrase) => {
        return copyFromSource(phrase, phrase.selectedPluralIndex)
      }})

    case COPY_FROM_SOURCE:
      const { phraseId, sourceIndex } = action.payload
      return updatePhrase(phraseId, {$apply: (phrase) => {
        return copyFromSource(phrase, sourceIndex)
      }})

    case COPY_GLOSSARY_TERM:
      return updatePhrase(state.selectedPhraseId, {$apply: phrase => {
        return insertTextAtRange(phrase, action.payload,
          state.selectedTextRange)
      }})

    case COPY_SUGGESTION:
      const { suggestion } = action
      return updatePhrase(state.selectedPhraseId, {$apply: phrase => {
        return copyFromSuggestion(phrase, suggestion)
      }})

    case PHRASE_DETAIL_REQUEST:
      return update({
        fetchingDetail: {$set: true}
      })

    case PHRASE_LIST_REQUEST:
      if (action.meta.filter) {
        return update({
          fetchingFilteredList: {$set: true}
        })
      } else {
        return update({
          fetchingList: {$set: true}
        })
      }

    case PENDING_SAVE_INITIATED:
      return updatePhrase(action.phraseId, {
        pendingSave: {$set: undefined}
      })

    case PHRASE_LIST_SUCCESS:
      if (action.meta.filter) {
        if (action.meta.timestamp > state.filteredListTimestamp) {
          return update({
            fetchingFilteredList: {$set: false},
            filteredListTimestamp: {$set: action.meta.timestamp},
            inDocFiltered: {
              [action.payload.docId]: {$set: action.payload.phraseList}
            }
          })
        } else {
          // stale search, ignore results
          return state
        }
      } else {
        const showingFiltered = getHasAdvancedFilter({ phrases: state })
        const selectedPhraseId = showingFiltered
          // this list not visible, keep same value
          ? state.selectedPhraseId
          // list is showing, select a visible phrase
          : decideSelectedPhrase(state, action.payload.phraseList)
        return update({
          fetchingList: {$set: false},
          inDoc: {[action.payload.docId]: {$set: action.payload.phraseList}},
          selectedPhraseId: {$set: selectedPhraseId}
        })
      }

    case PHRASE_LIST_FAILURE:
      if (action.meta.filter) {
        return update({
          fetchingFilteredList: {$set: false}
        })
      } else {
        return update({
          fetchingList: {$set: false}
        })
      }

    case PHRASE_DETAIL_SUCCESS:
      // TODO this shallow merge will lose data from other locales
      //      ideally replace source and locale that was looked up, leaving
      //      others unchanged (depending on caching policy)
      return update({
        fetchingDetail: {$set: false},
        detail: {$merge: action.payload}
      })

    case PHRASE_TEXT_SELECTION_RANGE:
      return update({
        selectedTextRange: {$set: action.payload}
      })

    case QUEUE_SAVE:
      return updatePhrase(action.phraseId, {
        pendingSave: {$set: action.saveInfo}
      })

    case SAVE_FINISHED:
      const phrase = state.detail[action.phraseId]
      const { newTranslations } = phrase
      return updatePhrase(action.phraseId, {
        inProgressSave: {$set: undefined},
        // FIXME check whether this should be action.translations instead
        translations: {$set: newTranslations},
        // TODO same as inProgressSave.status unless the server adjusted it
        status: {$set: action.status},
        revision: {$set: action.revision}
      })

    case SAVE_INITIATED:
      return updatePhrase(action.phraseId, {
        inProgressSave: {$set: action.saveInfo}
      })

    case SELECT_PHRASE:
      return selectPhrase(state, action.payload)

    case SELECT_PHRASE_SPECIFIC_PLURAL:
      const withNewPluralIndex = updatePhrase(action.payload.phraseId, {
        selectedPluralIndex: {$set: action.payload.index}
      })
      return selectPhrase(withNewPluralIndex, action.payload.phraseId)

    case SET_SAVE_AS_MODE:
      return update({
        saveAsMode: {$set: action.payload}
      })

    case TRANSLATION_TEXT_INPUT_CHANGED:
      return update({
        detail: {
          [action.id]: {
            newTranslations: {
              [action.index]: {$set: action.text}
            }
          }
        }
      })

    case UNDO_EDIT:
      return updatePhrase(state.selectedPhraseId, {$apply: (phrase) => {
        return updateObject(phrase, {
          newTranslations: {$set: [...phrase.translations]}
        })
      }})

    case MOVE_NEXT:
      return changeSelectedIndex(index => index + 1)

    case MOVE_PREVIOUS:
      return changeSelectedIndex(index => index - 1)
  }

  return state

  /**
   * Apply the given commands to state.
   *
   * Just a shortcut to avoid having to pass state to update over and over.
   */
  function update (commands) {
    // FIXME update to version that does not lose reference equality when
    //       setting an identical object
    //       see: https://github.com/facebook/react/pull/4968
    return updateObject(state, commands)
  }

  /**
  * Apply commands to the indicated phrase detail.
  *
  * Returns state with just the indicated phrase changed.
  */
  function updatePhrase (phraseId, commands) {
    return update({
      detail: {
        [phraseId]: {$apply: (phrase) => {
          return updateObject(phrase, commands)
        }}
      }
    })
  }

  function updatePageIndex (newPageIndex) {
    const oldPageIndex = state.paging.pageIndex
    return oldPageIndex === newPageIndex
      ? state
      : update({ paging: { pageIndex: {$set: newPageIndex} } })
  }

  /**
  * Generate a state with a different phrase selected.
  *
  * @param state
  * @param action
  * @param indexUpdateCallback produces new index based on previous index
  * @returns {*}
  */
  function changeSelectedIndex (indexUpdateCallback) {
    const { docId } = action.getState().context
    const { inDoc, inDocFiltered, filter, selectedPhraseId } = state
    const phrases = hasAdvancedFilter(filter.advanced)
      ? inDocFiltered[docId] : inDoc[docId]

    const currentIndex = phrases.findIndex(x => x.id === selectedPhraseId)

    const newIndex = indexUpdateCallback(currentIndex)
    const indexOutOfBounds = newIndex < 0 || newIndex >= phrases.length
    if (!indexOutOfBounds && newIndex !== currentIndex) {
      const moveToId = phrases[newIndex].id
      return selectPhrase(state, moveToId)
    }

    return state
  }

  /**
  * Select a given phrase and ensure the correct page is showing.
  *
  * CAUTION: this calculates max page index from the previous state,
  *   so do not use this after updating page index.
  */
  function selectPhrase (state, phraseId) {
    const { countPerPage, pageIndex } = state.paging
    const phrases = getFilteredPhrases(action.getState())

    const phraseIndex = phrases.findIndex(x => x.id === phraseId)
    const desiredPageIndex = phraseIndex === -1
      // Just go to valid page nearest current page when selected phrase is
      // invisible. Ideal would be page that shows the phrase nearest the
      // selected one in the unfiltered document, but that is complicated.
      ? clamp(pageIndex, 0, getMaxPageIndex(action.getState()))
      : Math.floor(phraseIndex / countPerPage)

    return updateObject(state, {
      selectedPhraseId: {$set: phraseId},
      paging: {
        pageIndex: {$set: desiredPageIndex}
      }
    })
  }
}

function copyFromSource (phrase, sourceIndex) {
  const { selectedPluralIndex, sources, shouldGainFocus } = phrase
  const focusedTranslationIndex = selectedPluralIndex || 0

  // FIXME use clamp from lodash (when lodash >= 4.0)
  const sourceIndexToCopy =
    sourceIndex < sources.length
      ? sourceIndex
      : sources.length - 1
  const sourceToCopy = sources[sourceIndexToCopy]

  const focusId = (shouldGainFocus || 0) + 1
  return updateObject(phrase, {
    shouldGainFocus: {$set: focusId},
    newTranslations: {
      // $splice represents an array of calls to Array.prototype.splice
      // with an array of params for each call
      $splice: [[focusedTranslationIndex, 1, sourceToCopy]]
    }
  })
}

function copyFromSuggestion (phrase, suggestion) {
  const { selectedPluralIndex, shouldGainFocus } = phrase
  var targets = suggestion.targetContents

  // TODO ensure selectedPluralIndex is always set (in phrase reducer),
  //      so the default of 0 is only sepecified in one place.
  const focusedTranslationIndex = selectedPluralIndex || 0
  const targetIndexToCopy = focusedTranslationIndex < targets.length
    ? focusedTranslationIndex : targets.length - 1

  const focusId = (shouldGainFocus || 0) + 1
  return updateObject(phrase, {
    shouldGainFocus: {$set: focusId},
    newTranslations: {
      // $splice represents an array of calls to Array.prototype.splice
      // with an array of params for each call
      $splice: [[focusedTranslationIndex, 1, targets[targetIndexToCopy]]]
    }
  })
}

function insertTextAtRange (phrase, text, {start, end}) {
  const { newTranslations, selectedPluralIndex, shouldGainFocus } = phrase
  const focusedTranslationIndex = selectedPluralIndex || 0

  const original = newTranslations[focusedTranslationIndex]
  const updated = replaceRange(original, text, start, end)

  // TODO set selected index as end of inserted range
  //      (likely needs to use refs on the component)
  // const cursorPositionAfter = start + text.length

  // The textarea is focused when shouldGainFocus has a changed truthy value
  // so incrementing will lead to focus being gained.
  const focusId = (shouldGainFocus || 0) + 1
  return updateObject(phrase, {
    shouldGainFocus: {$set: focusId},
    newTranslations: {
      // $splice represents an array of calls to Array.prototype.splice
      // with an array of params for each call
      $splice: [[focusedTranslationIndex, 1, updated]]
    }
  })
}

/* Decide which phrase should be selected out of a given phrase list
 *
 * Considers which phrase was selected and which page is showing.
 * Does not filter the list, so pass in filtered phrase list if that is showing.
 */
function decideSelectedPhrase (state, phraseList) {
  const { selectedPhraseId, paging: { countPerPage, pageIndex } } = state

  const selectedPhraseIndex =
    findIndex(phraseList, ({ id }) => id === selectedPhraseId)

  if (selectedPhraseIndex > -1) {
    // A present phrase is already selected, just may be wrong page number
    // TODO set page number? Would need to return full state
    return selectedPhraseId
  }

  const maxPageIndex = Math.ceil(phraseList.length / countPerPage) - 1
  const clampedPageIndex = Math.min(pageIndex, maxPageIndex)

  const firstIndex = clampedPageIndex * countPerPage
  return phraseList[firstIndex].id
}

export default composeReducers(
  phraseReducer,
  subReducer('filter', phraseFilterReducer)
)

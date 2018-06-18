// @ts-nocheck
import React from 'react'
import { handleActions } from 'redux-actions'
import update from 'immutability-helper'
import phraseFilterReducer, { defaultState as defaultFilterState }
  from './phrase-filter-reducer'
import { composeReducers, subReducer } from 'redux-sac'
import {
  CLAMP_PAGE,
  UPDATE_PAGE
} from '../../actions/controls-header-actions'
import { SEVERITY } from '../../../actions/common-actions'
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
  SAVE_FAILED,
  SAVE_INITIATED,
  SAVE_CONFLICT,
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
import { findIndex, isEmpty } from 'lodash'

/* eslint-disable max-len */

// FIXME this reducer is too big. See if it can be split up.

// TODO use lodash when upgraded
// clamps a number within the inclusive lower and upper bounds
function clamp (number, lower, upper) {
  return Math.max(lower, Math.min(number, upper))
}

export const defaultState = {
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
  notification: undefined,
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

export const phraseReducer = handleActions({
  // TODO use local selector if possible, so that top-level state is not needed
  [CLAMP_PAGE]: (state, { getState }) => update(state, {
    paging: {
      pageIndex:
        {$set: clamp(state.paging.pageIndex, 0, getMaxPageIndex(getState()))}
    }
  }),
  [UPDATE_PAGE]: (state, { payload }) => {
    // TODO use selector
    const oldPageIndex = state.paging.pageIndex
    return oldPageIndex === payload
      ? state
      : update(state, { paging: { pageIndex: {$set: payload} } })
  },

  [CANCEL_EDIT]: state => update(state, {
    selectedPhraseId: {$set: undefined},
    detail: {
      [state.selectedPhraseId]: {
        // Discard any newTranslations that were entered.
        newTranslations:
          {$set: state.detail[state.selectedPhraseId].translations}
      }
    }
  }),

  [COPY_FROM_ALIGNED_SOURCE]: (state, {getState}) => updatePhrase(
    state, getSelectedDocId(getState()), state.selectedPhraseId,
    {$apply: phrase => copyFromSource(phrase, phrase.selectedPluralIndex)
  }),

  [COPY_FROM_SOURCE]: (state, {getState, payload: {phraseId, sourceIndex}}) =>
    updatePhrase(state, getSelectedDocId(getState()), phraseId,
      {$apply: (phrase) => copyFromSource(phrase, sourceIndex)}),

  [COPY_GLOSSARY_TERM]: (state, { getState, payload }) => updatePhrase(
    state, getSelectedDocId(getState()), state.selectedPhraseId,
    {$apply: phrase =>
      insertTextAtRange(phrase, payload, state.selectedTextRange)}),

  [COPY_SUGGESTION]: (state, { getState, payload }) => updatePhrase(
    state, getSelectedDocId(getState()), state.selectedPhraseId,
    {$apply: phrase => copyFromSuggestion(phrase, payload)
  }),

  [PHRASE_DETAIL_REQUEST]: state => update(state, {
    fetchingDetail: {$set: true}}),

  [PHRASE_LIST_REQUEST]: (state, { meta: { filter } }) => filter
    ? update(state, { fetchingFilteredList: {$set: true} })
    : update(state, { fetchingList: {$set: true} }),

  [PENDING_SAVE_INITIATED]: (state, { getState, payload }) =>
    updatePhrase(state, getSelectedDocId(getState()), payload,
    { pendingSave: {$set: undefined} }),

  [PHRASE_LIST_SUCCESS]: (state, {
    payload: { docId, phraseList },
    meta: { filter, timestamp }
  }) => {
    if (filter) {
      return timestamp > state.filteredListTimestamp
        ? update(state, {
          fetchingFilteredList: {$set: false},
          filteredListTimestamp: {$set: timestamp},
          inDocFiltered: {
            [docId]: {$set: phraseList}
          }
        })
        // stale search, ignore results
        : state
    } else {
      // FIXME use a local selector here
      const showingFiltered = getHasAdvancedFilter({ phrases: state })
      const selectedPhraseId = showingFiltered
        // this list not visible, keep same value
        ? state.selectedPhraseId
        // list is showing, select a visible phrase
        : decideSelectedPhrase(state, phraseList)
      return update(state, {
        fetchingList: {$set: false},
        inDoc: {[docId]: {$set: phraseList}},
        selectedPhraseId: {$set: selectedPhraseId}
      })
    }
  },

  [PHRASE_LIST_FAILURE]: (state, { meta: { filter } }) => filter
    ? update(state, { fetchingFilteredList: {$set: false} })
    : update(state, { fetchingList: {$set: false} }),

  [PHRASE_DETAIL_SUCCESS]: (state, { payload }) => update(state, {
    fetchingDetail: {$set: false},
    // TODO this shallow merge will lose data from other locales
    //      ideally replace source and locale that was looked up, leaving
    //      others unchanged (depending on caching policy)
    detail: {$merge: payload}
  }),

  [PHRASE_TEXT_SELECTION_RANGE]: (state, { payload }) =>
    update(state, { selectedTextRange: {$set: payload} }),

  [QUEUE_SAVE]: (state, {getState, payload: {phraseId, saveInfo}}) =>
    updatePhrase(state, getSelectedDocId(getState()), phraseId,
      {pendingSave: {$set: saveInfo}}),

  [SAVE_FINISHED]: (state, {getState, payload: {phraseId, status, revision}}) =>
    updatePhrase(state, getSelectedDocId(getState()), phraseId,
      {
        inProgressSave: {$set: undefined},
        // FIXME check whether this should be translations from the action
        translations: {$set: state.detail[phraseId].newTranslations},
        // TODO same as inProgressSave.status unless the server adjusted it
        status: {$set: status},
        revision: {$set: revision}
      }),

  [SAVE_FAILED]: (state, { payload: { phraseId, saveInfo, response } }) =>
    update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Save Translation Failed`,
          description:
            <p>
              Unable to save phraseId {phraseId}
              {isEmpty(saveInfo.translations)
                ? null
                : ` as ${saveInfo.translations[0]}`}
              <br />
              Status {response.status} {response.statusText}
            </p>
        }
      },
      detail: {
        [phraseId]: {
          inProgressSave: { $set: undefined }
        }
      }
    }),

  [SAVE_CONFLICT]: (state, { payload: { phraseId, saveInfo, response } }) =>
    update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Save Translation Failed`,
          description:
            <p>
              Unable to save phraseId {phraseId}
              {isEmpty(saveInfo.translations)
                ? null
                : ` as ${saveInfo.translations[0]}`}
              <br />
              Status {response.status} {response.statusText}
            </p>
        }
      },
      detail: {
        [phraseId]: {
          inProgressSave: { $set: undefined }
        }
      }
    }),

  [SAVE_INITIATED]: (state, {getState, payload: {phraseId, saveInfo}}) =>
    updatePhrase(state, getSelectedDocId(getState()), phraseId,
      {inProgressSave: {$set: saveInfo}}),

  // TODO see if this is possible without using global state
  [SELECT_PHRASE]: (state, { getState, payload }) =>
    selectPhrase(state, getState(), payload),

  [SELECT_PHRASE_SPECIFIC_PLURAL]:
    (state, {getState, payload: {phraseId, index}}) => selectPhrase(
      updatePhrase(state, getSelectedDocId(getState()), phraseId,
        {selectedPluralIndex: {$set: index}}), getState(), phraseId),

  [SET_SAVE_AS_MODE]: (state, { payload }) =>
    update(state, { saveAsMode: {$set: payload} }),

  [TRANSLATION_TEXT_INPUT_CHANGED]: (state, {payload: {id, index, text}}) =>
    update(state, {
      detail: { [id]: { newTranslations: { [index]: {$set: text} } } }
    }),

  [UNDO_EDIT]: (state, {getState}) => updatePhrase(state,
    getSelectedDocId(getState()), state.selectedPhraseId,
    {
      $apply: (phrase) => update(phrase, {
        newTranslations: {$set: [...phrase.translations]}
      })
    }),

  [MOVE_NEXT]: (state, { getState }) =>
    changeSelectedIndex(state, getState(), index => index + 1),

  [MOVE_PREVIOUS]: (state, { getState }) =>
    changeSelectedIndex(state, getState(), index => index - 1)
}, defaultState)

/**
* Generate a state with a different phrase selected.
*
* @param state
* @param action
* @param indexUpdateCallback produces new index based on previous index
* @returns {*}
*/
function changeSelectedIndex (state, globalState, indexUpdateCallback) {
  const { docId } = globalState.context
  const { inDoc, inDocFiltered, filter, selectedPhraseId } = state
  const phrases = hasAdvancedFilter(filter.advanced)
    ? inDocFiltered[docId] : inDoc[docId]
  const phrasesFiltered = filter.status.all
    ? phrases
    : phrases.filter((phrase) => {
      return filter.status[phrase.status]
    })
  const currentIndex = phrasesFiltered.findIndex(x => x.id === selectedPhraseId)

  const newIndex = indexUpdateCallback(currentIndex)
  const indexOutOfBounds = newIndex < 0 || newIndex >= phrasesFiltered.length
  if (!indexOutOfBounds && newIndex !== currentIndex) {
    const moveToId = phrasesFiltered[newIndex].id
    return selectPhrase(state, globalState, moveToId)
  }

  return state
}

/**
* Select a given phrase and ensure the correct page is showing.
*
* CAUTION: this calculates max page index from the previous state,
*   so do not use this after updating page index.
*/
function selectPhrase (state, globalState, phraseId) {
  const { countPerPage, pageIndex } = state.paging
  const phrases = getFilteredPhrases(globalState)

  const phraseIndex = phrases.findIndex(x => x.id === phraseId)
  const desiredPageIndex = phraseIndex === -1
    // Just go to valid page nearest current page when selected phrase is
    // invisible. Ideal would be page that shows the phrase nearest the
    // selected one in the unfiltered document, but that is complicated.
    ? clamp(pageIndex, 0, getMaxPageIndex(globalState))
    : Math.floor(phraseIndex / countPerPage)

  return update(state, {
    selectedPhraseId: {$set: phraseId},
    paging: {
      pageIndex: {$set: desiredPageIndex}
    }
  })
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
  return update(phrase, {
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
  return update(phrase, {
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
  return update(phrase, {
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

/**
* Apply commands to the indicated phrase detail.
*
* Returns state with just the indicated phrase changed.
*/
function updatePhrase (state, docId, phraseId, commands) {
  const phraseList = state.inDoc[docId]
  const phraseIndex = phraseList.findIndex(x => x.id === phraseId)
  const updatedPhrase = update(state.detail[phraseId], commands)

  return update(state, {
    detail: {
      [phraseId]: {$set: updatedPhrase}
    },
    inDoc: {
      [docId]: {
        $set: update(phraseList,
          {[phraseIndex]: {status: {$set: updatedPhrase.status}}})
      }
    }
  })
}

function getSelectedDocId (state) {
  return state.headerData.context.selectedDoc.id
}

export default composeReducers(
  phraseReducer,
  subReducer('filter', phraseFilterReducer)
)

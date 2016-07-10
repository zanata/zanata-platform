import updateObject from 'react-addons-update'
import {
  CLAMP_PAGE,
  UPDATE_PAGE
} from '../actions/controlsHeaderActions'
import {
  CANCEL_EDIT,
  COPY_FROM_ALIGNED_SOURCE,
  COPY_FROM_SOURCE,
  FETCHING_PHRASE_DETAIL,
  FETCHING_PHRASE_LIST,
  PENDING_SAVE_INITIATED,
  PHRASE_LIST_FETCHED,
  PHRASE_DETAIL_FETCHED,
  QUEUE_SAVE,
  SAVE_FINISHED,
  SAVE_INITIATED,
  SELECT_PHRASE,
  SELECT_PHRASE_SPECIFIC_PLURAL,
  TRANSLATION_TEXT_INPUT_CHANGED,
  UNDO_EDIT
} from '../actions/phrases'
import { COPY_SUGGESTION } from '../actions/suggestions'
import {
  calculateMaxPageIndex,
  calculateMaxPageIndexFromState,
  getFilteredPhrasesFromState
} from '../utils/filter-paging-util'
import { SET_SAVE_AS_MODE } from '../actions/editorShortcuts'
import { MOVE_NEXT, MOVE_PREVIOUS } from '../actions/phraseNavigation'

// TODO use lodash when upgraded
// clamps a number within the inclusive lower and upper bounds
function clamp (number, lower, upper) {
  return Math.max(lower, Math.min(number, upper))
}

const defaultState = {
  fetchingList: false,
  fetchingDetail: false,
  saveAsMode: false,
  // expected shape: { [docId1]: [{ id, resId, status }, ...], [docId2]: [...] }
  inDoc: {},
  // expected shape: { [phraseId1]: phrase-object, [phraseId2]: ..., ...}
  detail: {},
  selectedPhraseId: undefined,
  paging: {
    countPerPage: 20,
    pageIndex: 0
  }
}

const phraseReducer = (state = defaultState, action) => {
  switch (action.type) {
    case CLAMP_PAGE:
      return update({
        paging: {
          pageIndex: {$set: clamp(state.paging.pageIndex, 0,
            calculateMaxPageIndexFromState(action.getState()))}
        }
      })

    case UPDATE_PAGE:
      return updatePageIndex(action.page)

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
      const { phraseId, sourceIndex } = action
      return updatePhrase(phraseId, {$apply: (phrase) => {
        return copyFromSource(phrase, sourceIndex)
      }})

    case COPY_SUGGESTION:
      const { suggestion } = action
      return updatePhrase(state.selectedPhraseId, {$apply: phrase => {
        return copyFromSuggestion(phrase, suggestion)
      }})

    case FETCHING_PHRASE_DETAIL:
      return update({
        fetchingDetail: {$set: true}
      })

    case FETCHING_PHRASE_LIST:
      return update({
        fetchingList: {$set: true}
      })

    case PENDING_SAVE_INITIATED:
      return updatePhrase(action.phraseId, {
        pendingSave: {$set: undefined}
      })

    case PHRASE_LIST_FETCHED:
    // select the first phrase if there is one
      const selectedPhraseId = action.phraseList.length &&
        action.phraseList[0].id
      return update({
        fetchingList: {$set: false},
        inDoc: {[action.docId]: {$set: action.phraseList}},
        selectedPhraseId: {$set: selectedPhraseId},
        docStatus: {$set: action.statusList}

      })

    case PHRASE_DETAIL_FETCHED:
      // TODO this shallow merge will lose data from other locales
      //      ideally replace source and locale that was looked up, leaving
      //      others unchanged (depending on caching policy)
      return update({
        fetchingDetail: {$set: false},
        detail: {$merge: action.phrases}
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
      return selectPhrase(state, action.phraseId)

    case SELECT_PHRASE_SPECIFIC_PLURAL:
      const withNewPluralIndex = updatePhrase(action.phraseId, {
        selectedPluralIndex: {$set: action.index}
      })
      return selectPhrase(withNewPluralIndex, action.phraseId)

    case SET_SAVE_AS_MODE:
      return update({
        saveAsMode: {$set: action.active}
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

    if (oldPageIndex !== newPageIndex) {
      return update({
        paging: {
          pageIndex: {
            $set: newPageIndex
          }
        }
      })
    }
    return state
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
    const { inDoc, selectedPhraseId } = state
    const phrases = inDoc[docId]
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
  */
  function selectPhrase (state, phraseId) {
    const { countPerPage, pageIndex } = state.paging
    const phrases = getFilteredPhrasesFromState(action.getState())

    const phraseIndex = phrases.findIndex(x => x.id === phraseId)
    const desiredPageIndex = phraseIndex === -1
      // Just go to valid page nearest current page when selected phrase is
      // invisible. Ideal would be page that shows the phrase nearest the
      // selected one in the unfiltered document, but that is complicated.
      ? clamp(pageIndex, 0, calculateMaxPageIndex(phrases, countPerPage))
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

export default phraseReducer

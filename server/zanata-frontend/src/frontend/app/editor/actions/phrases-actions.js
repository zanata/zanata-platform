import { savePhrase } from '../api'
import { toggleDropdown } from '.'
import {
  PHRASE_DETAIL_SUCCESS,
  PHRASE_DETAIL_FAILURE,
  COPY_FROM_SOURCE,
  COPY_FROM_ALIGNED_SOURCE,
  CANCEL_EDIT,
  UNDO_EDIT,
  SELECT_PHRASE,
  SELECT_PHRASE_SPECIFIC_PLURAL,
  PHRASE_TEXT_SELECTION_RANGE,
  TRANSLATION_TEXT_INPUT_CHANGED,
  QUEUE_SAVE,
  SAVE_INITIATED,
  PENDING_SAVE_INITIATED,
  SAVE_FINISHED
} from './phrases-action-types'
import {
  defaultSaveStatus,
  transUnitStatusToPhraseStatus
} from '../utils/status-util'
import { hasTranslationChanged } from '../utils/phrase-util'

// detail for phrases has been fetched from API
export function phraseDetailFetched (phrases) {
  return { type: PHRASE_DETAIL_SUCCESS, phrases: phrases }
}

export function phraseDetailFetchFailed (error) {
  return { type: PHRASE_DETAIL_FAILURE, error: error }
}

/**
 * Copy from source text to the focused translation input.
 * Only change the input text, not the saved translation value.
 */
export function copyFromSource (phraseId, sourceIndex) {
  return { type: COPY_FROM_SOURCE,
           phraseId: phraseId,
           sourceIndex: sourceIndex
         }
}

/**
 * Copy the source that is at the same plural index to the focused translation
 * plural. If there are not enough source plural forms, the highest one is used.
 */
export function copyFromAlignedSource () {
  return { type: COPY_FROM_ALIGNED_SOURCE }
}

/**
 * Stop editing the currently focused phrase and discard all entered text.
 * After this action, no phrase should be in editing state.
 */
export function cancelEdit () {
  return {
    type: CANCEL_EDIT
  }
}

/**
 * Discard all entered text for the currently selected phrase, reverting to
 * whatever translations are currently saved.
 * After this action, a phrase may still be in editing state.
 */
export function undoEdit () {
  return {
    type: UNDO_EDIT
  }
}

/**
 * Set the selected phrase to the given ID.
 * Only one phrase is selected at a time.
 */
export function selectPhrase (phraseId) {
  return (dispatch) => {
    dispatch(savePreviousPhraseIfChanged(phraseId))
    dispatch({type: SELECT_PHRASE, phraseId})
  }
}

/**
 * Select a phrase and set which of its plurals is selected.
 * The selected plural index should persist even when the phrase loses focus
 * and gains it back again (unless it gains focus from a different plural form
 * being specifically targeted).
 */
export function selectPhrasePluralIndex (phraseId, index) {
  return (dispatch) => {
    dispatch(savePreviousPhraseIfChanged(phraseId))
    dispatch({ type: SELECT_PHRASE_SPECIFIC_PLURAL, phraseId, index })
  }
}

function savePreviousPhraseIfChanged (phraseId) {
  return (dispatch, getState) => {
    const previousPhraseId = getState().phrases.selectedPhraseId
    if (previousPhraseId && previousPhraseId !== phraseId) {
      const previousPhrase = getState().phrases.detail[previousPhraseId]
      if (previousPhrase && hasTranslationChanged(previousPhrase)) {
        dispatch(savePhraseWithStatus(previousPhrase,
          defaultSaveStatus(previousPhrase)))
      }
    }
  }
}

/**
 * Use to broadcast the cursor location or selection within the focused
 * translation text.
 *
 * @param start position of cursor or beginning of range
 * @param end position of cursor (if no range is selected) or end of range
 */
export function phraseTextSelectionRange (start, end) {
  return {
    type: PHRASE_TEXT_SELECTION_RANGE,
    payload: {
      start,
      end
    }
  }
}

// User has typed/pasted/etc. text for a translation (not saved yet)
export function translationTextInputChanged (id, index, text) {
  return {
    type: TRANSLATION_TEXT_INPUT_CHANGED,
    id: id,
    index: index,
    text: text
  }
}

export function savePhraseWithStatus (phrase, status) {
  return (dispatch, getState) => {
    // save dropdowns (and others) should always close when save starts.
    dispatch(toggleDropdown(undefined))

    const stateBefore = getState()
    const saveInfo = {
      localeId: stateBefore.context.lang,
      status,
      translations: phrase.newTranslations
    }

    const inProgressSave =
      stateBefore.phrases.detail[phrase.id].inProgressSave

    if (inProgressSave) {
      dispatch(queueSave(phrase.id, saveInfo))
      // done for now, save will initiate when inProgressSave completes
      return
    }

    doSave(saveInfo)

    /**
     * Perform a save with the given info, and recursively start next save if
     * one has queued when the save finishes.
     */
    function doSave (saveInfo) {
      // fetch a new phrase copy each time so revision and queued saves are
      // are correct.
      const currentPhrase = getState().phrases.detail[phrase.id]
      dispatch(saveInitiated(phrase.id, saveInfo))
      savePhrase(currentPhrase, saveInfo)
        .then(response => {
          if (isErrorResponse(response)) {
            console.error('Failed to save phrase')
            // TODO dispatch an error about save failure
            //      this should remove the inProgressSave data
            // FIXME make phraseSaveFailed exist
            // dispatch(phraseSaveFailed(currentPhrase, saveInfo))
          } else {
            response.json().then(({ revision, status }) => {
              dispatch(saveFinished(phrase.id, status, revision))
            })
          }
          startPendingSaveIfPresent(currentPhrase)
        })
    }

    function startPendingSaveIfPresent (currentPhrase) {
      const pendingSave = currentPhrase.pendingSave
      if (pendingSave) {
        dispatch(pendingSaveInitiated(currentPhrase.id))
        doSave(pendingSave)
      }
    }
  }
}

function queueSave (phraseId, saveInfo) {
  return {
    type: QUEUE_SAVE,
    phraseId,
    saveInfo
  }
}

function saveInitiated (phraseId, saveInfo) {
  return {
    type: SAVE_INITIATED,
    phraseId,
    saveInfo
  }
}

function pendingSaveInitiated (phraseId) {
  return {
    type: PENDING_SAVE_INITIATED,
    phraseId
  }
}

// FIXME should use status and serverStatus to disambiguate
//       (these would be separate types if there were types.)
function saveFinished (phraseId, transUnitStatus, revision) {
  return {
    type: SAVE_FINISHED,
    phraseId,
    status: transUnitStatusToPhraseStatus(transUnitStatus),
    revision
  }
}

function isErrorResponse (response) {
  return response.status >= 400
}

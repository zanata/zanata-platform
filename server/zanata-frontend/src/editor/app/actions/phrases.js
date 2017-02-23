import { fetchPhraseList, fetchPhraseDetail, savePhrase } from '../api'
import { toggleDropdown } from '.'
import { isUndefined, mapValues, slice } from 'lodash'
import {
  defaultSaveStatus,
  STATUS_NEW,
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_NEEDS_WORK_SERVER
} from '../utils/status'
import { hasTranslationChanged } from '../utils/phrase'

export const FETCHING_PHRASE_LIST = Symbol('FETCHING_PHRASE_LIST')

// API lookup of the list of phrase id + phrase status for the current document
export function requestPhraseList (projectSlug, versionSlug, lang, docId,
                                   paging) {
  return (dispatch, getState) => {
    dispatch({ type: FETCHING_PHRASE_LIST })

    if (isUndefined(lang)) {
      // cannot request phrases without a language
      dispatch(phraseListFetchFailed(
        new Error('No language selected, cannot fetch phrases.')))
      return
    }

    fetchPhraseList(projectSlug, versionSlug, lang, docId)
      .then(response => {
        if (isErrorResponse(response)) {
          // TODO error detail from actual response object
          dispatch(phraseListFetchFailed(
            new Error('Failed to fetch phrase list')))
          // can also throw to fail the promise
          return Promise.reject('')
        }
        return response.json()
      })
      .then(statusList => {
        // TODO statusList has status format from server, convert
        dispatch(phraseListFetched(docId, statusList, statusList.map(phrase => {
          return {
            ...phrase,
            status: transUnitStatusToPhraseStatus(phrase.status)
          }
        })))
        dispatch(fetchPhraseDetails(statusList, lang, paging))
      })
  }
}

export function fetchPhraseDetails (statusList, language, paging) {
  return (dispatch) => {
    const startIndex = paging.pageIndex * paging.countPerPage
    const endIndex = paging.countPerPage + startIndex
    const ids = slice(statusList, startIndex, endIndex).map(phrase => {
      return phrase.id
    })
    dispatch(requestPhraseDetail(language, ids))
  }
}

// new phrase list has been fetched from API
export const PHRASE_LIST_FETCHED = Symbol('PHRASE_LIST_FETCHED')
export function phraseListFetched (docId, statusList, phraseList) {
  return {
    type: PHRASE_LIST_FETCHED,
    docId: docId,
    phraseList: phraseList,
    statusList: statusList
  }
}

export const PHRASE_LIST_FETCH_FAILED = Symbol('PHRASE_LIST_FETCH_FAILED')
export function phraseListFetchFailed (error) {
  return { type: PHRASE_LIST_FETCH_FAILED, error: error }
}

export const FETCHING_PHRASE_DETAIL = Symbol('FETCHING_PHRASE_DETAIL')
// API lookup of the detail for a given set of phrases (by id)
export function requestPhraseDetail (localeId, phraseIds) {
  return (dispatch) => {
    dispatch({ type: FETCHING_PHRASE_DETAIL })
    fetchPhraseDetail(localeId, phraseIds)
      .then(response => {
        if (isErrorResponse(response)) {
          // TODO error info from actual response object
          dispatch(phraseDetailFetchFailed(
            new Error('Failed to fetch phrase detail')))
        }
        return response.json()
      })
      .then(transUnitDetail => {
        dispatch(
          phraseDetailFetched(
            // phraseDetail
            transUnitDetailToPhraseDetail(transUnitDetail, localeId)
          )
        )
      })
  }
}

/**
 * Convert the TransUnit response objects to the Phrase structure that
 * is needed for the component tree.
 */
function transUnitDetailToPhraseDetail (transUnitDetail, localeId) {
  return mapValues(transUnitDetail, (transUnit, index) => {
    const {
      content,
      contents,
      id,
      msgctxt,
      plural,
      sourceComment,
      sourceFlags,
      sourceReferences,
      wordCount
    } = transUnit.source
    const trans = transUnit[localeId]
    const translations = extractTranslations(trans)

    return {
      id: parseInt(index, 10),
      resId: id,
      msgctxt,
      sourceComment,
      sourceFlags,
      sourceReferences,
      plural,
      sources: plural ? contents : [content],
      translations,
      newTranslations: [...translations],
      status: transUnitStatusToPhraseStatus(trans && trans.state),
      revision: trans && trans.revision ? parseInt(trans.revision, 10) : 0,
      wordCount: parseInt(wordCount, 10),
      lastModifiedBy: trans && trans.translator && trans.translator.name,
      lastModifiedTime: trans && trans.lastModifiedTime &&
        new Date(trans.lastModifiedTime)
    }
  })
}

/**
 * Get translations from a TransUnit in a consistent form (array of strings)
 *
 * This will always return an Array<String>, but the array may be empty.
 */
function extractTranslations (trans) {
  if (!trans) {
    return []
  }
  return trans.content ? [trans.content]
    // Array.slice() efficiently makes a copy of the array.
    : (trans.contents ? trans.contents.slice() : [])
}

/**
 * Correct the incoming status keys to match what is expected in
 * the app. No status is assumed to mean new.
 *
 * Expect: untranslated/needswork/translated/approved
 */
function transUnitStatusToPhraseStatus (mixedCaseStatus) {
  const status = mixedCaseStatus && mixedCaseStatus.toLowerCase()
  if (!status || status === STATUS_NEW) {
    return STATUS_UNTRANSLATED
  }
  if (status === STATUS_NEEDS_WORK_SERVER) {
    return STATUS_NEEDS_WORK
  }
  // remaining status should be ok just lowercased
  return status
}

// detail for phrases has been fetched from API
export const PHRASE_DETAIL_FETCHED = Symbol('PHRASE_DETAIL_FETCHED')
export function phraseDetailFetched (phrases) {
  return { type: PHRASE_DETAIL_FETCHED, phrases: phrases }
}

export const PHRASE_DETAIL_FETCH_FAILED = Symbol('PHRASE_DETAIL_FETCH_FAILED')
export function phraseDetailFetchFailed (error) {
  return { type: PHRASE_DETAIL_FETCH_FAILED, error: error }
}

/**
 * Copy from source text to the focused translation input.
 * Only change the input text, not the saved translation value.
 */
export const COPY_FROM_SOURCE = Symbol('COPY_FROM_SOURCE')
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
export const COPY_FROM_ALIGNED_SOURCE = Symbol('COPY_FROM_ALIGNED_SOURCE')
export function copyFromAlignedSource () {
  return { type: COPY_FROM_ALIGNED_SOURCE }
}

/**
 * Stop editing the currently focused phrase and discard all entered text.
 * After this action, no phrase should be in editing state.
 */
export const CANCEL_EDIT = Symbol('CANCEL_EDIT')
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
export const UNDO_EDIT = Symbol('UNDO_EDIT')
export function undoEdit () {
  return {
    type: UNDO_EDIT
  }
}

/**
 * Set the selected phrase to the given ID.
 * Only one phrase is selected at a time.
 */
export const SELECT_PHRASE = Symbol('SELECT_PHRASE')
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
export const SELECT_PHRASE_SPECIFIC_PLURAL =
  Symbol('SELECT_PHRASE_SPECIFIC_PLURAL')
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

// User has typed/pasted/etc. text for a translation (not saved yet)
export const TRANSLATION_TEXT_INPUT_CHANGED =
  Symbol('TRANSLATION_TEXT_INPUT_CHANGED')
export function translationTextInputChanged (id, index, text) {
  return {
    type: TRANSLATION_TEXT_INPUT_CHANGED,
    id: id,
    index: index,
    text: text
  }
}

// TODO check if this type label is ever actually used
export const SAVE_PHRASE_WITH_STATUS = Symbol('SAVE_PHRASE_WITH_STATUS')
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

export const QUEUE_SAVE = Symbol('QUEUE_SAVE')
function queueSave (phraseId, saveInfo) {
  return {
    type: QUEUE_SAVE,
    phraseId,
    saveInfo
  }
}

export const SAVE_INITIATED = Symbol('SAVE_INITIATED')
function saveInitiated (phraseId, saveInfo) {
  return {
    type: SAVE_INITIATED,
    phraseId,
    saveInfo
  }
}

export const PENDING_SAVE_INITIATED = Symbol('PENDING_SAVE_INITIATED')
function pendingSaveInitiated (phraseId) {
  return {
    type: PENDING_SAVE_INITIATED,
    phraseId
  }
}

// FIXME should use status and serverStatus to disambiguate
//       (these would be separate types if there were types.)
export const SAVE_FINISHED = Symbol('SAVE_FINISHED')
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

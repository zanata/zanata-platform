import { compact, every, isEmpty, isUndefined } from 'lodash'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED
} from './status-util'

const nullToEmpty = (value) => {
  return value || ''
}

export function getSaveButtonStatus (phrase) {
  if (hasNoTranslation(phrase)) {
    return STATUS_UNTRANSLATED
  } else if (hasEmptyTranslation(phrase)) {
    return STATUS_NEEDS_WORK
  } else if (hasTranslationChanged(phrase)) {
    return STATUS_TRANSLATED
  } else {
    return phrase.status
  }
}

export function hasTranslationChanged (phrase) {
  // on Firefox with input method turned on,
  // when hitting tab it seems to turn undefined value into ''

  // Iterating newTranslations since those are guaranteed to exist for all
  // plural forms. translations can be just an empty array.
  const allSame = every(phrase.newTranslations,
      function (translation, index) {
        return nullToEmpty(translation) ===
            nullToEmpty(phrase.translations[index])
      })

  return !allSame
}

export function hasNoTranslation (phrase) {
  return isEmpty(compact(phrase.newTranslations))
}

export function hasEmptyTranslation (phrase) {
  return compact(phrase.newTranslations).length !==
      phrase.newTranslations.length
}

/**
 * Execute a callback when phrase detail is available.
 *
 * @param getState the getState function from redux thunk
 * @param phraseId id for the phrase to wait for
 * @param callback is invoked with the phrase detail
 * @param reps (optional) only try this many times, then invoke errorCallback
 * @param errorCallback (optional) invoked if the detail was not available after
 *                                 reps tries
 */
export function waitForPhraseDetail (getState, phraseId, callback, reps,
                                     errorCallback) {
  doWait(reps)

  function doWait (reps) {
    const phrase = getState().phrases.detail[phraseId]
    if (phrase) {
      callback(phrase)
    } else if (isUndefined(reps)) {
      doWait()
    } else if (reps > 0) {
      // FIXME need a better way than polling to trigger this search as soon
      //       as the phrase detail is available.
      setTimeout(() => {
        doWait(reps - 1)
      }, 500)
    } else {
      errorCallback()
    }
  }
}

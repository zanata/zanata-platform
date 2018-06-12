import { compact, every, isEmpty, isUndefined } from 'lodash'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED
} from './status-util'
import {Phrase} from './phrase'

const nullToEmpty = (value: string) => {
  return value || ''
}

export function getSaveButtonStatus (phrase: Phrase) {
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

export function hasTranslationChanged (phrase: Phrase) {
  // on Firefox with input method turned on,
  // when hitting tab it seems to turn undefined value into ''

  // Iterating newTranslations since those are guaranteed to exist for all
  // plural forms. translations can be just an empty array.
  const allSame = every(phrase.newTranslations,
      function (translation, index) {
        return nullToEmpty(translation) ===
            // @ts-ignore
            nullToEmpty(phrase.translations[index])
      })

  return !allSame
}

export function hasNoTranslation (phrase: Phrase) {
  return isEmpty(compact(phrase.newTranslations))
}

export function hasEmptyTranslation (phrase: Phrase) {
  return compact(phrase.newTranslations).length !==
    // @ts-ignore
      phrase.newTranslations.length
}

/**
 * Execute a callback when phrase detail is available.
 *
 * @param getState the getState function from redux thunk
 * @param phraseId id for the phrase to wait for
 * @param callback is invoked with the phrase detail
 * @param maxReps (optional) only try this many times, then invoke errorCallback
 * @param errorCallback (optional) invoked if the detail was not available after
 *                                 reps tries
 */
export function waitForPhraseDetail (getState: () => any, phraseId: string, callback: (phrase: Phrase) => void,
    maxReps: number, errorCallback: () => void) {

  doWait(maxReps)

  function doWait (reps?: number) {
    const phrase: Phrase|undefined = getState().phrases.detail[phraseId]
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

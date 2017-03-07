import { compact, every, isEmpty } from 'lodash'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED
} from './status'

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
  var allSame = every(phrase.translations,
      function (translation, index) {
        return nullToEmpty(translation) ===
            nullToEmpty(phrase.newTranslations[index])
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

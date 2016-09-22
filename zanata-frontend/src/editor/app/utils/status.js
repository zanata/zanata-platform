import { without } from 'lodash'
import {
  hasEmptyTranslation,
  hasNoTranslation,
  hasTranslationChanged
} from './phrase'

export const STATUS_NEW = 'new'
export const STATUS_UNTRANSLATED = 'untranslated'
export const STATUS_NEEDS_WORK = 'needswork'
// the server provides this value instead of the one expected by this app
export const STATUS_NEEDS_WORK_SERVER = 'needreview'
export const STATUS_TRANSLATED = 'translated'
export const STATUS_APPROVED = 'approved'
export const STATUS_REJECTED = 'rejected'

/**
 * Get a string representing the status that should be
 * the selected status on the save button dropdown.
 *
 * Restricts the status to only valid values, based on
 * which translations are currently entered.
 */
export function defaultSaveStatus (phrase) {
  if (hasNoTranslation(phrase)) {
    // only possible state is untranslated
    return STATUS_UNTRANSLATED
  } else if (hasEmptyTranslation(phrase)) {
    return STATUS_NEEDS_WORK
  } else if (hasTranslationChanged(phrase)) {
    // TODO may also need to handle 'approved' and 'rejected'
    //      when user is a reviewer and in review mode.
    return STATUS_TRANSLATED
  } else {
    // TODO when phrase status is a simple value,
    //      change to just return the simple value
    return phrase.status
  }
}

export function nonDefaultValidSaveStatuses (phrase) {
  const all = allValidSaveStatuses(phrase)
  return without(all, defaultSaveStatus(phrase))
}

/**
 * Get a list of all the translation statuses
 * that would be valid to save the current new
 * translations of a phrase.
 */
function allValidSaveStatuses (phrase) {
  if (hasNoTranslation(phrase)) {
    // only possible state is untranslated
    return [STATUS_UNTRANSLATED]
  } else if (hasEmptyTranslation(phrase)) {
    return [STATUS_NEEDS_WORK]
  } else if
    (phrase.status === STATUS_REJECTED && !hasTranslationChanged(phrase)) {
    // rejected state cannot be saved in this editor yet, but should display
    // as a disabled button until the text is changed.
    return [STATUS_REJECTED, STATUS_TRANSLATED, STATUS_NEEDS_WORK]
  } else {
    // TODO also need to handle 'approved' and 'rejected'
    //      when user is a reviewer and in review mode
    return [STATUS_TRANSLATED, STATUS_NEEDS_WORK]
  }
}

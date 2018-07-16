import { without } from 'lodash'
import {
  hasEmptyTranslation,
  hasNoTranslation,
  hasTranslationChanged,
} from './phrase-util'
import * as phrases from './phrase'
import {Phrase, Status} from './phrase'

// deprecated:
export const STATUS_NEW = phrases.STATUS_NEW
export const STATUS_UNTRANSLATED = phrases.STATUS_UNTRANSLATED
export const STATUS_NEEDS_WORK = phrases.STATUS_NEEDS_WORK
export const STATUS_NEEDS_WORK_SERVER = phrases.STATUS_NEEDS_WORK_SERVER
export const STATUS_TRANSLATED = phrases.STATUS_TRANSLATED
export const STATUS_APPROVED = phrases.STATUS_APPROVED
export const STATUS_REJECTED = phrases.STATUS_REJECTED

export const SERVER_STATUS_NEW = 'New'
export const SERVER_STATUS_UNTRANSLATED = 'Untranslated'
export const SERVER_STATUS_NEEDS_WORK = 'NeedReview'
export const SERVER_STATUS_TRANSLATED = 'Translated'
export const SERVER_STATUS_APPROVED = 'Approved'
export const SERVER_STATUS_REJECTED = 'Rejected'

/**
 * Get a string representing the status that should be
 * the selected status on the save button dropdown.
 *
 * Restricts the status to only valid values, based on
 * which translations are currently entered.
 */
export function defaultSaveStatus(phrase: Phrase) {
  if (hasNoTranslation(phrase)) {
    // only possible state is untranslated
    return STATUS_UNTRANSLATED
  } else if (hasEmptyTranslation(phrase) || phrase.errors) {
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

// @ts-ignore any
export function nonDefaultValidSaveStatuses (phrase: Phrase, permissions) {
  const all = allValidSaveStatuses(phrase, permissions)
  return without(all, defaultSaveStatus(phrase))
}

/**
 * Get a list of all the translation statuses
 * that would be valid to save the current new
 * translations of a phrase.
 */
// @ts-ignore any
function allValidSaveStatuses (phrase: Phrase, permissions): Status[] {
  if (!permissions.translator && !permissions.reviewer) {
    // User does not have privileges for any operations.
    return []
  }
  if (hasNoTranslation(phrase)) {
    // only possible state is untranslated
    return [STATUS_UNTRANSLATED]
  } else if (hasEmptyTranslation(phrase) || phrase.errors) {
    return [STATUS_NEEDS_WORK]
  } else if
    (phrase.status === STATUS_REJECTED && !hasTranslationChanged(phrase)) {
    return [STATUS_REJECTED, STATUS_TRANSLATED, STATUS_NEEDS_WORK]
  } else if (permissions.reviewer && phrase.status === STATUS_TRANSLATED) {
    return [STATUS_APPROVED, STATUS_NEEDS_WORK, STATUS_REJECTED]
  } else {
    // TODO also need to handle 'approved' and 'rejected'
    //      when user is a reviewer and in review mode
    return [STATUS_TRANSLATED, STATUS_NEEDS_WORK]
  }
}

/**
 * Correct the incoming status keys to match what is expected in
 * the app. No status is assumed to mean new.
 *
 * Expect: untranslated/needswork/translated/approved
 */
export function transUnitStatusToPhraseStatus (mixedCaseStatus: string) {
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

export const phraseStatusToTransUnitStatus = {
  STATUS_NEEDS_WORK: SERVER_STATUS_NEEDS_WORK,
  STATUS_TRANSLATED: SERVER_STATUS_TRANSLATED,
  STATUS_APPROVED: SERVER_STATUS_APPROVED,
  STATUS_REJECTED: SERVER_STATUS_REJECTED,
  STATUS_UNTRANSLATED: SERVER_STATUS_UNTRANSLATED,
  STATUS_NEW: SERVER_STATUS_NEW
}

import {tuple} from '../../utils/tuple'

export const statuses = tuple(
  'untranslated',
  'needswork',
  'translated',
  'approved',
  'rejected'
)
export type Status = typeof statuses[number]

export const STATUS_NEW = 'new'
export const STATUS_UNTRANSLATED = 'untranslated'
export const STATUS_NEEDS_WORK = 'needswork'
// the server provides this value instead of the one expected by this app
export const STATUS_NEEDS_WORK_SERVER = 'needreview'
export const STATUS_TRANSLATED = 'translated'
export const STATUS_APPROVED = 'approved'
export const STATUS_REJECTED = 'rejected'

export interface Phrase {
  status?: Status,
  resId?: string,
  id?: string,
  plural?: boolean,
  revision?: number,
  wordCount?: number,
  lastModifiedBy?: string,
  lastModifiedTime?: string,
  selectedPluralIndex?: number,
  sources?: string[],
  translations?: string[],
  newTranslations?: string[],
  inProgressSave?: boolean,
  comments?: number | string,
  errors?: boolean,
}

// TODO: Determine why this differs from Phrase naming
export interface SelectedPhrase {
  msgctxt?: string,
  resId: string,
  sourceComment?: number | string,
  sourceFlags?: string,
  sourceReferences?: string,
  lastModifiedBy?: string,
  lastModifiedTime?: string,
  revision?: number
}

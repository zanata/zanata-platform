
// FIXME replace these completely with the new ones
export const FETCHING_PHRASE_LIST = Symbol('FETCHING_PHRASE_LIST')
export const PHRASE_LIST_FETCHED = Symbol('PHRASE_LIST_FETCHED')
export const PHRASE_LIST_FETCH_FAILED = Symbol('PHRASE_LIST_FETCH_FAILED')
// the new ones
export const PHRASE_LIST_REQUEST = 'PHRASE_LIST_REQUEST'
export const PHRASE_LIST_SUCCESS = 'PHRASE_LIST_SUCCESS'
export const PHRASE_LIST_FAILED = 'PHRASE_LIST_FAILED'

export const FETCHING_PHRASE_DETAIL = Symbol('FETCHING_PHRASE_DETAIL')
export const PHRASE_DETAIL_FETCHED = Symbol('PHRASE_DETAIL_FETCHED')
export const PHRASE_DETAIL_FETCH_FAILED = Symbol('PHRASE_DETAIL_FETCH_FAILED')

export const COPY_FROM_SOURCE = Symbol('COPY_FROM_SOURCE')
export const COPY_FROM_ALIGNED_SOURCE = Symbol('COPY_FROM_ALIGNED_SOURCE')
export const CANCEL_EDIT = Symbol('CANCEL_EDIT')
export const UNDO_EDIT = Symbol('UNDO_EDIT')
export const SELECT_PHRASE = Symbol('SELECT_PHRASE')
export const SELECT_PHRASE_SPECIFIC_PLURAL =
  Symbol('SELECT_PHRASE_SPECIFIC_PLURAL')
export const PHRASE_TEXT_SELECTION_RANGE = Symbol('PHRASE_TEXT_SELECTION_RANGE')
export const TRANSLATION_TEXT_INPUT_CHANGED =
  Symbol('TRANSLATION_TEXT_INPUT_CHANGED')
// TODO check if this type label is ever actually used

export const SAVE_PHRASE_WITH_STATUS = Symbol('SAVE_PHRASE_WITH_STATUS')
export const QUEUE_SAVE = Symbol('QUEUE_SAVE')
export const SAVE_INITIATED = Symbol('SAVE_INITIATED')
export const PENDING_SAVE_INITIATED = Symbol('PENDING_SAVE_INITIATED')
export const SAVE_FINISHED = Symbol('SAVE_FINISHED')

/* Filtering */
export const RESET_STATUS_FILTERS = 'RESET_STATUS_FILTERS'
export const UPDATE_STATUS_FILTER = 'UPDATE_STATUS_FILTER'

export const TOGGLE_ADVANCED_PHRASE_FILTERS = 'TOGGLE_ADVANCED_PHRASE_FILTERS'
export const UPDATE_PHRASE_FILTER = 'UPDATE_PHRASE_FILTER'

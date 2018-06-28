/*
 * Update page number from query string whenever it changes.
 *
 * Note: all page navigation should be done via query string so that
 * this does not become unnecessarily complex.
 */

import watch from './watch'
import { getLocationPageNumber } from '../selectors'
import { UPDATE_PAGE } from '../actions/controls-header-actions'
import { createAction } from 'redux-actions'

// @ts-ignore any
export const watchQueryStringPageNumber = store => {
  const watcher = watch('page-number > watchQueryStringPageNumber')(
    () => getLocationPageNumber(store.getState()))
  // @ts-ignore any
  store.subscribe(watcher(pageNumber => {
    // @ts-ignore
    store.dispatch(createAction(UPDATE_PAGE)(pageNumber))
  }))
}

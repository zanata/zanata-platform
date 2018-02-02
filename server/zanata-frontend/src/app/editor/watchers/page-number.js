// @ts-nocheck
/*
 * Update page number from query string whenever it changes.
 *
 * Note: all page navigation should be done via query string so that
 * this does not become unnecessarily complex.
 */

import watch from './watch'
import { getLocationPageNumber } from '../selectors'
import { UPDATE_PAGE } from '../actions/controls-header-actions'
import { createAction } from 'typesafe-actions'

export const watchQueryStringPageNumber = store => {
  const watcher = watch('page-number > watchQueryStringPageNumber')(
    () => getLocationPageNumber(store.getState()))
  store.subscribe(watcher(pageNumber => {
    store.dispatch(createAction(UPDATE_PAGE)(pageNumber))
  }))
}

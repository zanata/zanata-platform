import {
  calculateMaxPageIndexFromState
} from '../utils/filter-paging-util'
import { replaceRouteQuery } from '../utils/RoutingHelpers'

/* Adjust the page number so it is in the valid range.
 * Dispatch after changing the filter.
 */
export const CLAMP_PAGE = Symbol('CLAMP_PAGE')
export function clampPage () {
  return { type: CLAMP_PAGE }
}

export const UPDATE_PAGE = Symbol('UPDATE_PAGE')

export function firstPage () {
  return (dispatch, getState) => {
    const pageIndex = 0
    updatePage(dispatch, getState().routing.location, pageIndex)
  }
}

export function nextPage () {
  return (dispatch, getState) => {
    const currentPageIndex = getState().phrases.paging.pageIndex
    const pageIndex = Math.min(currentPageIndex + 1,
      calculateMaxPageIndexFromState(getState()))
    updatePage(dispatch, getState().routing.location, pageIndex)
  }
}

export function previousPage () {
  return (dispatch, getState) => {
    const currentPageIndex = getState().phrases.paging.pageIndex
    const pageIndex = Math.max(currentPageIndex - 1, 0)
    updatePage(dispatch, getState().routing.location, pageIndex)
  }
}

export function lastPage () {
  return (dispatch, getState) => {
    const pageIndex = calculateMaxPageIndexFromState(getState())
    updatePage(dispatch, getState().routing.location, pageIndex)
  }
}

function updatePage (dispatch, location, pageIndex) {
  replaceRouteQuery(location, {
    page: pageIndex + 1
  })
  dispatch({type: UPDATE_PAGE, page: pageIndex})
}

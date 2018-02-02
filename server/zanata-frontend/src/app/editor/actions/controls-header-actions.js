import { getLocation, getPageIndex, getMaxPageIndex } from '../selectors'
import { replaceRouteQuery } from '../utils/RoutingHelpers'
import { createAction } from 'typesafe-actions'

/* Adjust the page number so it is in the valid range.
 * Dispatch after changing the filter.
 */
export const CLAMP_PAGE = 'CLAMP_PAGE'
export const clampPage = createAction(CLAMP_PAGE)

export const UPDATE_PAGE = 'UPDATE_PAGE'

export function firstPage () {
  return (dispatch, getState) => {
    const pageIndex = 0
    updatePage(dispatch, getLocation(getState()), pageIndex)
  }
}

export function nextPage () {
  return (dispatch, getState) => {
    const currentPageIndex = getPageIndex(getState())
    const pageIndex = Math.min(currentPageIndex + 1,
      getMaxPageIndex(getState()))
    updatePage(dispatch, getLocation(getState()), pageIndex)
  }
}

export function previousPage () {
  return (dispatch, getState) => {
    const currentPageIndex = getPageIndex(getState())
    const pageIndex = Math.max(currentPageIndex - 1, 0)
    updatePage(dispatch, getLocation(getState()), pageIndex)
  }
}

export function lastPage () {
  return (dispatch, getState) => {
    const pageIndex = getMaxPageIndex(getState())
    updatePage(dispatch, getLocation(getState()), pageIndex)
  }
}

function updatePage (dispatch, location, pageIndex) {
  replaceRouteQuery(location, {
    page: pageIndex + 1
  })
  dispatch(createAction(UPDATE_PAGE)(pageIndex))
}

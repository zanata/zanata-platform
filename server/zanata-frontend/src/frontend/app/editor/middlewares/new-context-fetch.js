import stateChangeDispatchMiddleware from './state-change-dispatch'
import { createAction } from 'redux-actions'
import {
  fetchHeaderInfo,
  selectDoc,
  selectLocale
} from '../actions/header-actions'
import { UPDATE_PAGE } from '../actions/controls-header-actions'
import { every, isUndefined, max, negate } from 'lodash'

/**
 * Get page query from url, and check is integer and >= 0
 * @param state
 * @returns {number}
 */
const getPageIndexFromQuery = (state) => {
  return state.routing.locationBeforeTransitions
    ? state.routing.locationBeforeTransitions.query.page
      ? max([
        parseInt(state.routing.locationBeforeTransitions.query.page, 10) - 1,
        0
      ])
      : 0
    : 0
}

/**
 * Middleware to fetch new data when the context changes.
 *
 * e.g.
 *  - when selected doc ID changes, fetch text flow info and details
 *  - when selected locale changes, fetch text flow info and details
 *  - when page changes, fetch text flows details
 *  - when the project or version change, fetch a new document list
 */
const fetchDocsMiddleware = stateChangeDispatchMiddleware(
  // FIXME replace with watcher
  (dispatch, oldState, newState) => {
    const { lang, docId } = newState.context

    const docChanged = oldState.context.docId !== docId
    const localeChanged = oldState.context.lang !== lang

    const newPageIndex = getPageIndexFromQuery(newState)
    const oldPageIndex = getPageIndexFromQuery(oldState)

    const updatePage = oldPageIndex !== newPageIndex
    if (docChanged || localeChanged) {
      if (docChanged) {
        // FIXME looks like probably duplicate state
        dispatch(selectDoc(docId))
      }
      if (localeChanged) {
        // FIXME looks like more duplicate state here
        // FIXME just use selected locale from context.lang if possible
        dispatch(selectLocale(lang))
      }
      dispatch(createAction(UPDATE_PAGE)(newPageIndex))
    } else if (updatePage) {
      dispatch(createAction(UPDATE_PAGE)(newPageIndex))
    }
  },
  // Fetch new header data only when the full workspace is first known
  (dispatch, oldState, newState) => {
    function hasAllContextInfo (state) {
      const { projectSlug, versionSlug, docId } = state.context
      return every([projectSlug, versionSlug, docId], negate(isUndefined))
    }

    if (!hasAllContextInfo(oldState) && hasAllContextInfo(newState)) {
      const { projectSlug, versionSlug, lang, docId } = newState.context
      dispatch(fetchHeaderInfo(projectSlug, versionSlug, docId, lang))
    }
  }
)

export default fetchDocsMiddleware

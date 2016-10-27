import stateChangeDispatchMiddleware from './state-change-dispatch'
import { requestDocumentList } from '../actions'
import { requestPhraseList, fetchPhraseDetails } from '../actions/phrases'
import { selectDoc, selectLocale } from '../actions/headerActions'
import { UPDATE_PAGE } from '../actions/controlsHeaderActions'
import { max } from 'lodash'

/**
 * Get page query from url, and check is integer and >= 0
 * @param state
 * @returns {number}
 */
const getPageIndexFromQuery = (state) => {
  return state.routing.location.query.page
    ? max([parseInt(state.routing.location.query.page, 10) - 1, 0]) : 0
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
  (dispatch, oldState, newState) => {
    const pre = oldState.context
    const post = newState.context
    const needDocs = pre.projectSlug !== post.projectSlug ||
                     pre.versionSlug !== post.versionSlug
    if (needDocs) {
      dispatch(requestDocumentList())
    }
  },
  (dispatch, oldState, newState) => {
    const docChanged = oldState.context.docId !== newState.context.docId
    const localeChanged = oldState.context.lang !== newState.context.lang

    const newPageIndex = getPageIndexFromQuery(newState)
    const oldPageIndex = getPageIndexFromQuery(oldState)

    const updatePage = oldPageIndex !== newPageIndex
    if (docChanged || localeChanged) {
      const { projectSlug, versionSlug, lang, docId } = newState.context
      const paging = {
        ...newState.phrases.paging,
        pageIndex: newPageIndex
      }
      if (docChanged) {
        dispatch(selectDoc(docId))
      }
      if (localeChanged) {
        dispatch(selectLocale(lang))
      }
      dispatch({type: UPDATE_PAGE, page: newPageIndex})
      dispatch(requestPhraseList(projectSlug, versionSlug, lang, docId, paging))
    } else if (updatePage) {
      const phraseState = newState.phrases
      const paging = {
        ...phraseState.paging,
        pageIndex: newPageIndex
      }
      dispatch({type: UPDATE_PAGE, page: newPageIndex})
      dispatch(fetchPhraseDetails(phraseState.docStatus,
        newState.context.lang, paging))
    }
  }
)

export default fetchDocsMiddleware

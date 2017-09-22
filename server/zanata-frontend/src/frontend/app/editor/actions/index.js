import { fetchDocuments } from '../api'
import { createAction } from 'redux-actions'
import {
  ROUTING_PARAMS_CHANGED,
  SET_SIDEBAR_VISIBILITY,
  TOGGLE_DROPDOWN,
  OPEN_DROPDOWN,
  CLOSE_DROPDOWN
} from './action-types'

export const routingParamsChanged = createAction(ROUTING_PARAMS_CHANGED)

/**
 * Every dropdown should have a reference-unique key.
 */
export const toggleDropdown = createAction(TOGGLE_DROPDOWN)
export const openDropdown = createAction(OPEN_DROPDOWN)
export const closeDropdown = createAction(CLOSE_DROPDOWN)

/**
 * Fetch the list of documents for the current project-version
 */
export function requestDocumentList () {
  return (dispatch, getState) => {
    const { projectSlug, versionSlug } = getState().context

    fetchDocuments(projectSlug, versionSlug)
    .then(response => {
      if (response.status >= 400) {
        console.error('Document list fetch failed', response)
        // FIXME implement this action
        // dispatch(documentListFetchFailed())
        return
      }
      return response.json()
    })
    .then(docList => {
      // expect docList to be an array of things like:
      // {
      //   contentType: 'text/plain',
      //   extensions: null,
      //   lang: 'en-US',
      //   name: 'hello.txt',
      //   revision: '1',
      //   type: 'FILE'
      // }

      // Probably want a cache like:
      // {
      //   projectA: {
      //     versionA: {
      //       documents: [...]
      //     }
      //   }
      // }

      // Then the context info is basically an address in that:
      // document = state.projects[projectSlug][versionSlug][docId]

      // Then for phrases, a cache list cache by document id
      // and a detail cache by phrase id

      // action includes project+version so that it can be stored in the right
      // cache location, and to avoid possible race conditions if the slugs
      // change quickly - the wrong doc list could be attached to a different
      // version that is selected, for example.
      dispatch(documentListFetched(projectSlug, versionSlug, docList))
    })
  }
}

// TODO replace all action type constants with symbols when the logger supports
//      them. See https://github.com/fcomb/redux-logger/issues/128
// export const DOCUMENT_LIST_FETCHED = Symbol('DOCUMENT_LIST_FETCHED')
export const DOCUMENT_LIST_FETCHED = Symbol('DOCUMENT_LIST_FETCHED')
export function documentListFetched (projectSlug, versionSlug, documents) {
  return {
    type: DOCUMENT_LIST_FETCHED,
    projectSlug,
    versionSlug,
    documents
  }
}

export const setSidebarVisibility = createAction(SET_SIDEBAR_VISIBILITY)

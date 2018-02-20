// @ts-nocheck
import {
  fetchStatistics,
  fetchLocales,
  fetchMyInfo,
  fetchProjectInfo,
  fetchDocuments,
  fetchVersionLocales,
  getTranslationPermission
} from '../api'
import {
  HIDE_SIDEBAR,
  TOGGLE_GLOSSARY,
  TOGGLE_INFO_PANEL,
  TOGGLE_HEADER,
  TOGGLE_KEY_SHORTCUTS,
  UI_LOCALES_FETCHED,
  CHANGE_UI_LOCALE,
  DOCUMENT_SELECTED,
  LOCALE_SELECTED,
  STATS_FETCHED,
  HEADER_DATA_FETCHED
} from './header-action-types'
import { some, curry, isEmpty } from 'lodash'
import { createAction } from 'redux-actions'
import { equals } from '../utils/string-utils'

export const hideSidebar = createAction(HIDE_SIDEBAR)
export const toggleGlossary = createAction(TOGGLE_GLOSSARY)
export const toggleInfoPanel = createAction(TOGGLE_INFO_PANEL)
export const toggleHeader = createAction(TOGGLE_HEADER)
export const toggleKeyboardShortcutsModal = createAction(TOGGLE_KEY_SHORTCUTS)

const unwrapResponse = (_dispatch, _errorMsg, response) => {
  if (response.status >= 400) {
    // FIXME replace with api middleware and use standard error handling
    // new Error(errorMsg))
  }
  return response.json()
}

export const uiLocaleFetched = createAction(UI_LOCALES_FETCHED)

export function fetchUiLocales () {
  return (dispatch) => {
    fetchLocales()
        .then(curry(unwrapResponse)(dispatch, 'fetch UI locales failed'))
        .then(uiLocales => dispatch(uiLocaleFetched(uiLocales)))
        .catch(err => {
          console.error('Failed to fetch UI locales', err)
          // FIXME replace with api middleware and use standard error handling
          // return {type: FETCH_FAILED, error: err}
        })
  }
}

export const changeUiLocale = createAction(CHANGE_UI_LOCALE)

const decodeDocId = (docId) => {
  return docId ? docId.replace(/,/g, '/') : docId
}

const hasCaseInsensitiveMatchingProp = (list, prop, matchedValue) => {
  return some(list, (item) => {
    return equals(item[prop], matchedValue, true)
  })
}

const containsDoc = (documents, docId) => {
  return hasCaseInsensitiveMatchingProp(documents, 'name', docId)
}

const containsLocale = (localeList, localeId) => {
  return hasCaseInsensitiveMatchingProp(localeList, 'localeId', localeId)
}

export const selectDoc = createAction(DOCUMENT_SELECTED)
export const selectLocale = createAction(LOCALE_SELECTED)
export const statsFetched = createAction(STATS_FETCHED)
export const headerDataFetched = createAction(HEADER_DATA_FETCHED)

// this is a get all action that will wait until all promises are resovled
export function fetchHeaderInfo (projectSlug, versionSlug, docId, localeId) {
  return (dispatch, _getState) => {
    const checkResponse = curry(unwrapResponse)(dispatch)

    dispatch(getTranslationPermission(localeId, projectSlug))

    // FIXME make the checkResponse just reject with the error code
    //       no need to handle error messages or anything.

    const docListPromise = fetchDocuments(projectSlug, versionSlug)
        .then(checkResponse('fetch document list failed'))
        .catch(e => {
          e.message = 'document list fetch failed: ' + e.message
          throw e
        })
    const projectInfoPromise = fetchProjectInfo(projectSlug)
        .then(checkResponse('fetch project info failed'))
        .catch(e => {
          e.message = 'project info fetch failed: ' + e.message
          throw e
        })
    const myInfoPromise = fetchMyInfo()
        .then(checkResponse('fetch my INFO failed'))
        .catch(e => {
          e.message = 'version locales fetch failed: ' + e.message
          throw e
        })
    const versionLocalesPromise = fetchVersionLocales(projectSlug, versionSlug)
        .then(checkResponse('fetch version locales failed'))
        .catch(e => {
          e.message = 'version locales fetch failed: ' + e.message
          throw e
        })

    // FIXME Split to separate handlers for easier debugging and maintenance.
    //       There is no real dependency for each of these requests to have to
    //       all complete before storing the relevant data.
    Promise.all([docListPromise, projectInfoPromise,
      myInfoPromise, versionLocalesPromise])
        .then((all) => {
          const documents = all[0]
          const projectInfo = all[1]
          const myInfo = all[2]
          const locales = all[3]

          if (isEmpty(documents)) {
            // redirect if no documents in version
            // FIXME implement message Handler
            // MessageHandler.displayError('No documents in ' +
            //    editorCtrl.context.projectSlug + ' : ' +
            //    editorCtrl.context.versionSlug)
            console.error(`No documents in ${projectSlug}:${versionSlug}`)
            return
          }
          if (isEmpty(locales)) {
            // FIXME implement message Handler
            // redirect if no supported locale in version
            // MessageHandler.displayError('No supported locales in ' +
            //    editorCtrl.context.projectSlug + ' : ' +
            //    editorCtrl.context.versionSlug)
            console.error(
                `No supported locales in ${projectSlug}:${versionSlug}`)
            return
          }

          const data = {
            myInfo: myInfo,
            projectInfo: projectInfo,
            versionSlug: versionSlug,
            documents: documents,
            locales: locales
          }

          dispatch(headerDataFetched(data))

          return {documents, locales}
        })
        .then((docsAndLocales) => {
          const {documents, locales} = docsAndLocales
          // if docId is not defined in url, set it to be the first doc
          let selectedDocId = docId || documents[0].name
          selectedDocId = decodeDocId(selectedDocId)
          if (!containsDoc(documents, selectedDocId)) {
            selectedDocId = documents[0].name
          }

          // if localeId is not defined in url, set to first from list
          let selectedLocaleId = localeId || locales[0].localeId
          if (!containsLocale(locales, selectedLocaleId)) {
            selectedLocaleId = locales[0].localeId
          }

          // TODO trigger in new-context-fetch.js instead
          fetchStatistics(projectSlug, versionSlug, selectedDocId,
            selectedLocaleId)
            .then(checkResponse('fetch statistics failed'))
            .then(stats => dispatch(statsFetched(stats)))

          // dispatching selected doc and locale must happen after we compare
          // previous state otherwise it will not fetch stats
          dispatch(selectDoc(selectedDocId))
          // TODO pahuang this is commented out. implement this
          // transitionToEditorSelectedView()
          dispatch(selectLocale(selectedLocaleId))
        })
        .catch(err => {
          console.error('Failed to fetch all header info', err)
          // FIXME replace with api middleware and use standard error handling
          // return {type: FETCH_FAILED, error: err}
        })
  }
}

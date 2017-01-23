import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import { cloneDeep, includes, debounce, last } from 'lodash'
import { saveAs } from 'file-saver'
import { normalize } from 'normalizr'
import { GLOSSARY_TERM_ARRAY } from '../schemas.js'
import { replaceRouteQuery } from '../utils/RoutingHelpers'
import GlossaryHelper from '../utils/GlossaryHelper'
import {
  DEFAULT_LOCALE,
  getJsonHeaders,
  getHeaders,
  buildAPIRequest
} from './common'

export const FILE_TYPES = ['csv', 'po']
export const PAGE_SIZE_SELECTION = [20, 50, 100, 300, 500]
// 500 by default
export const PAGE_SIZE_DEFAULT = last(PAGE_SIZE_SELECTION)

export const GLOSSARY_PERMISSION_REQUEST = 'GLOSSARY_PERMISSION_REQUEST'
export const GLOSSARY_PERMISSION_SUCCESS = 'GLOSSARY_PERMISSION_SUCCESS'
export const GLOSSARY_PERMISSION_FAILURE = 'GLOSSARY_PERMISSION_FAILURE'
export const GLOSSARY_UPDATE_FILTER = 'GLOSSARY_UPDATE_FILTER'
export const GLOSSARY_UPDATE_LOCALE = 'GLOSSARY_UPDATE_LOCALE'
export const GLOSSARY_INIT_STATE_FROM_URL = 'GLOSSARY_INIT_STATE_FROM_URL'
export const GLOSSARY_TERMS_INVALIDATE = 'GLOSSARY_TERMS_INVALIDATE'
export const GLOSSARY_TERMS_REQUEST = 'GLOSSARY_TERMS_REQUEST'
export const GLOSSARY_TERMS_SUCCESS = 'GLOSSARY_TERMS_SUCCESS'
export const GLOSSARY_TERMS_FAILURE = 'GLOSSARY_TERMS_FAILURE'
export const GLOSSARY_DELETE_REQUEST = 'GLOSSARY_DELETE_REQUEST'
export const GLOSSARY_DELETE_SUCCESS = 'GLOSSARY_DELETE_SUCCESS'
export const GLOSSARY_DELETE_FAILURE = 'GLOSSARY_DELETE_FAILURE'
export const GLOSSARY_STATS_REQUEST = 'GLOSSARY_STATS_REQUEST'
export const GLOSSARY_STATS_SUCCESS = 'GLOSSARY_STATS_SUCCESS'
export const GLOSSARY_STATS_FAILURE = 'GLOSSARY_STATS_FAILURE'
export const GLOSSARY_SELECT_TERM = 'GLOSSARY_SELECT_TERM'
export const GLOSSARY_UPDATE_FIELD = 'GLOSSARY_UPDATE_FIELD'
export const GLOSSARY_RESET_TERM = 'GLOSSARY_RESET_TERM'
export const GLOSSARY_UPDATE_REQUEST = 'GLOSSARY_UPDATE_REQUEST'
export const GLOSSARY_UPDATE_SUCCESS = 'GLOSSARY_UPDATE_SUCCESS'
export const GLOSSARY_UPDATE_FAILURE = 'GLOSSARY_UPDATE_FAILURE'
export const GLOSSARY_UPLOAD_REQUEST = 'GLOSSARY_UPLOAD_REQUEST'
export const GLOSSARY_UPLOAD_SUCCESS = 'GLOSSARY_UPLOAD_SUCCESS'
export const GLOSSARY_UPLOAD_FAILURE = 'GLOSSARY_UPLOAD_FAILURE'
export const GLOSSARY_UPDATE_IMPORT_FILE = 'GLOSSARY_UPDATE_IMPORT_FILE'
export const GLOSSARY_UPDATE_IMPORT_FILE_LOCALE =
  'GLOSSARY_UPDATE_IMPORT_FILE_LOCALE'
export const GLOSSARY_TOGGLE_IMPORT_DISPLAY = 'GLOSSARY_TOGGLE_IMPORT_DISPLAY'
export const GLOSSARY_TOGGLE_EXPORT_DISPLAY = 'GLOSSARY_TOGGLE_EXPORT_DISPLAY'
export const GLOSSARY_UPDATE_EXPORT_TYPE = 'GLOSSARY_UPDATE_EXPORT_TYPE'
export const GLOSSARY_UPDATE_SORT = 'GLOSSARY_UPDATE_SORT'
export const GLOSSARY_TOGGLE_NEW_ENTRY_DISPLAY =
  'GLOSSARY_TOGGLE_NEW_ENTRY_DISPLAY'
export const GLOSSARY_TOGGLE_DELETE_ALL_ENTRIES_DISPLAY =
  'GLOSSARY_TOGGLE_DELETE_ALL_ENTRIES_DISPLAY'
export const GLOSSARY_CREATE_REQUEST = 'GLOSSARY_CREATE_REQUEST'
export const GLOSSARY_CREATE_SUCCESS = 'GLOSSARY_CREATE_SUCCESS'
export const GLOSSARY_CREATE_FAILURE = 'GLOSSARY_CREATE_FAILURE'
export const GLOSSARY_DELETE_ALL_REQUEST = 'GLOSSARY_DELETE_ALL_REQUEST'
export const GLOSSARY_DELETE_ALL_SUCCESS = 'GLOSSARY_DELETE_ALL_SUCCESS'
export const GLOSSARY_DELETE_ALL_FAILURE = 'GLOSSARY_DELETE_ALL_FAILURE'
export const GLOSSARY_EXPORT_REQUEST = 'GLOSSARY_EXPORT_REQUEST'
export const GLOSSARY_EXPORT_SUCCESS = 'GLOSSARY_EXPORT_SUCCESS'
export const GLOSSARY_EXPORT_FAILURE = 'GLOSSARY_EXPORT_FAILURE'

export const GLOSSARY_GET_QUALIFIED_NAME_REQUEST =
  'GLOSSARY_GET_QUALIFIED_NAME_REQUEST'
export const GLOSSARY_GET_QUALIFIED_NAME_SUCCESS =
  'GLOSSARY_GET_QUALIFIED_NAME_SUCCESS'
export const GLOSSARY_GET_QUALIFIED_NAME_FAILURE =
  'GLOSSARY_GET_QUALIFIED_NAME_FAILURE'

export const PROJECT_GET_DETAILS_REQUEST = 'PROJECT_GET_DETAILS_REQUEST'
export const PROJECT_GET_DETAILS_SUCCESS = 'PROJECT_GET_DETAILS_SUCCESS'
export const PROJECT_GET_DETAILS_FAILURE = 'PROJECT_GET_DETAILS_FAILURE'

export const glossaryUpdateLocale = createAction(GLOSSARY_UPDATE_LOCALE)
export const glossaryUpdateFilter = createAction(GLOSSARY_UPDATE_FILTER)
export const glossaryUpdateField = createAction(GLOSSARY_UPDATE_FIELD)
export const glossaryResetTerm = createAction(GLOSSARY_RESET_TERM)
export const updateSelectedTerm = createAction(GLOSSARY_SELECT_TERM)
export const glossaryUpdateImportFile =
  createAction(GLOSSARY_UPDATE_IMPORT_FILE)
export const glossaryToggleImportFileDisplay =
  createAction(GLOSSARY_TOGGLE_IMPORT_DISPLAY)
export const glossaryToggleExportFileDisplay =
  createAction(GLOSSARY_TOGGLE_EXPORT_DISPLAY)
export const glossaryUpdateExportType =
  createAction(GLOSSARY_UPDATE_EXPORT_TYPE)
export const glossaryUpdateImportFileLocale =
  createAction(GLOSSARY_UPDATE_IMPORT_FILE_LOCALE)
export const glossaryUpdateSort = createAction(GLOSSARY_UPDATE_SORT)
export const glossaryToggleNewEntryModal =
  createAction(GLOSSARY_TOGGLE_NEW_ENTRY_DISPLAY)
export const glossaryToggleDeleteAllEntriesModal =
  createAction(GLOSSARY_TOGGLE_DELETE_ALL_ENTRIES_DISPLAY)

const getGlossaryTerms = (state) => {
  const {
    src = DEFAULT_LOCALE.localeId,
    locale = '',
    filter = '',
    sort = '',
    qualifiedName
  } = state.glossary
  const query = state.routing.location.query

  let page = query.page ? parseInt(query.page) : 1
  page = page <= 1 ? 1 : page

  let pageSize = query.size ? parseInt(query.size) : PAGE_SIZE_DEFAULT
  pageSize = includes(PAGE_SIZE_SELECTION, pageSize)
    ? pageSize : PAGE_SIZE_DEFAULT

  const srcQuery = '?srcLocale=' + (src || DEFAULT_LOCALE.localeId)
  const localeQuery = locale ? `&transLocale=${locale}` : ''
  const pageQuery = `&page=${page}&sizePerPage=${pageSize}`
  const filterQuery = filter ? `&filter=${filter}` : ''
  const sortQuery = sort
    ? `&sort=${GlossaryHelper.convertSortToParam(sort)}` : ''
  const qualifiedNameQuery = '&qualifiedName=' + qualifiedName
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary/entries' + srcQuery +
    localeQuery + pageQuery + filterQuery + sortQuery + qualifiedNameQuery

  const apiTypes = [
    GLOSSARY_TERMS_REQUEST,
    {
      type: GLOSSARY_TERMS_SUCCESS,
      payload: (action, state, res) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            return normalize(json, { results: GLOSSARY_TERM_ARRAY })
          })
        }
      },
      meta: {
        page,
        receivedAt: Date.now()
      }
    },
    GLOSSARY_TERMS_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const getGlossaryStats = (dispatch, qualifiedName, resetTerms) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary/info?qualifiedName=' + qualifiedName
  const apiTypes = [
    GLOSSARY_STATS_REQUEST,
    {
      type: GLOSSARY_STATS_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          resetTerms && dispatch(getGlossaryTerms(state))
          return json
        })
      }
    },
    GLOSSARY_STATS_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const getPermission = (dispatch, qualifiedName) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/user/permission/glossary?qualifiedName=' + qualifiedName
  const apiTypes = [
    GLOSSARY_PERMISSION_REQUEST,
    {
      type: GLOSSARY_PERMISSION_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          dispatch(getGlossaryStats(dispatch, qualifiedName, true))
          return json
        })
      }
    },
    GLOSSARY_PERMISSION_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const getQualifiedName = (dispatch, projectSlug) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    (projectSlug ? '/projects/p/' + projectSlug + '/glossary/qualifiedName'
      : '/glossary/qualifiedName')

  const apiTypes = [
    GLOSSARY_GET_QUALIFIED_NAME_REQUEST,
    {
      type: GLOSSARY_GET_QUALIFIED_NAME_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          const qualifiedName = json.name
          dispatch(getPermission(dispatch, qualifiedName))
          return qualifiedName
        })
      }
    },
    GLOSSARY_GET_QUALIFIED_NAME_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const getProjectDetails = (projectSlug) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/projects/p/' + projectSlug

  const apiTypes = [
    PROJECT_GET_DETAILS_REQUEST,
    {
      type: PROJECT_GET_DETAILS_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          return json
        })
      }
    },
    PROJECT_GET_DETAILS_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

const importGlossaryFile = (dispatch, data, qualifiedName, srcLocaleId) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot + '/glossary'
  let formData = new FormData()
  formData.append('file', data.file, data.file.name)
  formData.append('fileName', data.file.name)
  formData.append('srcLocale', srcLocaleId)
  formData.append('qualifiedName', qualifiedName)
  if (data.transLocale) {
    formData.append('transLocale', data.transLocale.value)
  }

  const apiTypes = [
    GLOSSARY_UPLOAD_REQUEST,
    {
      type: GLOSSARY_UPLOAD_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          dispatch(getGlossaryStats(dispatch, qualifiedName, true))
          return json
        })
      }
    },
    GLOSSARY_UPLOAD_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'POST',
      getJsonHeaders(), apiTypes, formData)
  }
}

const createGlossaryTerm = (dispatch, qualifiedName, term) => {
  let headers = getJsonHeaders()
  headers['Content-Type'] = 'application/json'
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary/entries?locale=' + term.srcTerm.locale +
    '&qualifiedName=' + qualifiedName
  const entryDTO = GlossaryHelper.convertToDTO(term, qualifiedName)
  const apiTypes = [
    {
      type: GLOSSARY_CREATE_REQUEST,
      payload: (action, state) => {
        return term
      }
    },
    {
      type: GLOSSARY_CREATE_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          dispatch(getGlossaryStats(dispatch, qualifiedName, true))
          return json
        })
      }
    },
    GLOSSARY_CREATE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'POST', headers, apiTypes,
      JSON.stringify(entryDTO))
  }
}

const updateGlossaryTerm = (dispatch, qualifiedName, term, localeId,
    needRefresh) => {
  let headers = getJsonHeaders()
  headers['Content-Type'] = 'application/json'

  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary/entries?locale=' + localeId +
    '&qualifiedName=' + qualifiedName
  const entryDTO = GlossaryHelper.convertToDTO(term, qualifiedName)

  const apiTypes = [
    {
      type: GLOSSARY_UPDATE_REQUEST,
      payload: (action, state) => {
        return term
      }
    },
    {
      type: GLOSSARY_UPDATE_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          needRefresh &&
            dispatch(getGlossaryStats(dispatch, qualifiedName, false))
          return json
        })
      }
    },
    GLOSSARY_UPDATE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'POST', headers, apiTypes,
      JSON.stringify(entryDTO))
  }
}

const deleteGlossaryTerm = (dispatch, id, qualifiedName) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary/entries/' + id + '?qualifiedName=' + qualifiedName
  const apiTypes = [
    {
      type: GLOSSARY_DELETE_REQUEST,
      payload: (action, state) => {
        return id
      }
    },
    {
      type: GLOSSARY_DELETE_SUCCESS,
      payload: (action, state, res) => {
        return res.json().then((json) => {
          dispatch(getGlossaryStats(dispatch, state.glossary.qualifiedName,
            true))
          return json
        })
      }
    },
    GLOSSARY_DELETE_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'DELETE', getJsonHeaders(), apiTypes)
  }
}

const deleteAllGlossaryEntry = (dispatch, qualifiedName) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary?qualifiedName=' + qualifiedName
  const apiTypes = [
    {
      type: GLOSSARY_DELETE_ALL_REQUEST,
      payload: (action, state) => {
        return ''
      }
    },
    {
      type: GLOSSARY_DELETE_ALL_SUCCESS,
      payload: (action, state, res) => {
        return dispatch(getGlossaryStats(dispatch, qualifiedName, true))
      }
    },
    GLOSSARY_DELETE_ALL_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'DELETE', getJsonHeaders(), apiTypes)
  }
}

const glossaryExport = (type, qualifiedName) => {
  const endpoint = window.config.baseUrl + window.config.apiRoot +
    '/glossary/file?fileType=' + type + '&qualifiedName=' + qualifiedName
  let headers = getJsonHeaders()
  headers['Content-Type'] = 'application/octet-stream'
  const apiTypes = [
    {
      type: GLOSSARY_EXPORT_REQUEST,
      payload: (action, state) => {
        return ''
      }
    },
    {
      type: GLOSSARY_EXPORT_SUCCESS,
      payload: (action, state, res) => {
        return res.blob().then((blob) => {
          const selectedType = state.glossary.exportFile.type.value
          const fileName = 'glossary.' +
            (selectedType === 'po' ? 'zip' : selectedType)
          saveAs(blob, fileName)
        })
      }
    },
    GLOSSARY_EXPORT_FAILURE
  ]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getHeaders(), apiTypes)
  }
}

export const glossaryDownload = () => {
  return (dispatch, getState) => {
    dispatch(glossaryExport(getState().glossary.exportFile.type.value,
      getState().glossary.qualifiedName))
  }
}

export const glossaryInitStateFromUrl =
  createAction(GLOSSARY_INIT_STATE_FROM_URL)

export const glossaryInitialLoad = (projectSlug) => {
  return (dispatch, getState) => {
    const query = getState().routing.location.query
    dispatch(glossaryInitStateFromUrl({ query, projectSlug })).then(
    dispatch(getQualifiedName(dispatch, projectSlug)))
    if (projectSlug) {
      dispatch(getProjectDetails(projectSlug))
    }
  }
}

export const glossaryChangeLocale = (locale) => {
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.location, {
      locale: locale
    })
    dispatch(glossaryUpdateLocale(locale))
    dispatch(getGlossaryTerms(getState()))
  }
}

export const glossaryFilterTextChanged = (newFilter) => {
  return (dispatch, getState) => {
    if (!getState().glossary.termsLoading) {
      replaceRouteQuery(getState().routing.location, {
        filter: newFilter
      })
      dispatch(glossaryUpdateFilter(newFilter))
      dispatch(getGlossaryTerms(getState()))
    }
  }
}

export const glossaryDeleteTerm = (id) => {
  return (dispatch, getState) => {
    dispatch(deleteGlossaryTerm(dispatch, id,
      getState().glossary.qualifiedName))
  }
}

export const glossaryDeleteAll = () => {
  return (dispatch, getState) => {
    dispatch(deleteAllGlossaryEntry(dispatch,
      getState().glossary.qualifiedName))
  }
}

export const glossaryUpdateTerm = (term, needRefresh) => {
  return (dispatch, getState) => {
    const targetLocale = getState().glossary.locale || DEFAULT_LOCALE.localeId
    // do cloning to prevent changes in selectedTerm
    dispatch(updateGlossaryTerm(dispatch, getState().glossary.qualifiedName,
      cloneDeep(term), targetLocale, needRefresh))
  }
}

export const glossaryCreateNewEntry = (entry) => {
  return (dispatch, getState) => {
    dispatch(createGlossaryTerm(dispatch, getState().glossary.qualifiedName,
      entry))
  }
}

export const glossaryImportFile = () => {
  return (dispatch, getState) => {
    dispatch(importGlossaryFile(dispatch,
      getState().glossary.importFile,
      getState().glossary.qualifiedName,
      getState().glossary.stats.srcLocale.locale.localeId))
  }
}

export const glossarySelectTerm = (termId) => {
  return (dispatch, getState) => {
    const selectedTerm = getState().glossary.selectedTerm
    if (selectedTerm && selectedTerm.id !== termId) {
      const status = selectedTerm.status
      if (status && (status.isSrcModified || status.isTransModified)) {
        dispatch(glossaryUpdateTerm(selectedTerm, status.isTransModified))
      }
      dispatch(updateSelectedTerm(termId))
    }
  }
}

export const glossarySortColumn = (col) => {
  return (dispatch, getState) => {
    let sort = {}
    sort[col] = getState().glossary.sort[col]
      ? !getState().glossary.sort[col] : true

    replaceRouteQuery(getState().routing.location, {
      sort: GlossaryHelper.convertSortToParam(sort)
    })
    dispatch(glossaryUpdateSort(sort)).then(
      dispatch(getGlossaryTerms(getState()))
    )
  }
}

const delayGetGlossaryTerm = debounce((dispatch, state) =>
  dispatch(getGlossaryTerms(state)), 160)

export const glossaryGoFirstPage = (currentPage, totalPage) => {
  return (dispatch, getState) => {
    if (currentPage !== 1) {
      replaceRouteQuery(getState().routing.location, {page: 1})
      dispatch(getGlossaryTerms(getState()))
    }
  }
}

export const glossaryGoPreviousPage = (currentPage, totalPage) => {
  return (dispatch, getState) => {
    const newPage = currentPage - 1
    if (newPage >= 1) {
      replaceRouteQuery(getState().routing.location, {page: newPage})
      delayGetGlossaryTerm(dispatch, getState())
    }
  }
}

export const glossaryGoNextPage = (currentPage, totalPage) => {
  return (dispatch, getState) => {
    const newPage = currentPage + 1
    if (newPage <= totalPage) {
      replaceRouteQuery(getState().routing.location, {page: newPage})
      delayGetGlossaryTerm(dispatch, getState())
    }
  }
}

export const glossaryGoLastPage = (currentPage, totalPage) => {
  return (dispatch, getState) => {
    if (currentPage !== totalPage) {
      replaceRouteQuery(getState().routing.location, {page: totalPage})
      dispatch(getGlossaryTerms(getState()))
    }
  }
}

export const glossaryUpdatePageSize = (size) => {
  return (dispatch, getState) => {
    replaceRouteQuery(getState().routing.location, {page: 1, size: size})
    dispatch(getGlossaryTerms(getState()))
  }
}

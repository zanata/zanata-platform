/**
 * reducer for TMX components
 */
import { handleActions } from 'redux-actions'
import { saveAs } from 'file-saver'
import { isUndefined, cloneDeep } from 'lodash'

import {
  TMX_TYPE,
  SHOW_EXPORT_TMX_MODAL,
  GET_LOCALE_SUCCESS,
  GET_LOCALE_FAILURE,
  SET_INITIAL_STATE,
  GET_TMX_REQUEST,
  GET_TMX_SUCCESS,
  GET_TMX_FAILURE
} from '../actions/tmx-actions'

const buildTMXFileName = (project, version, srcLocale, locale) => {
  const p = !isUndefined(project) ? project : 'allProjects'
  const i = !isUndefined(version) ? version : 'allVersions'
  const sl = !isUndefined(srcLocale) ? srcLocale : 'allLocales'
  const l = !isUndefined(locale) ? locale : 'allLocales'
  return 'zanata-' + p + '-' + i + '-' + sl + '-' + l + '.tmx'
}

const tmx = handleActions(
  {
    [SET_INITIAL_STATE]: (state, action) => {
      const tmxExport = state.tmxExport
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          type: action.payload
        }
      }
    },
    [SHOW_EXPORT_TMX_MODAL]: (state, action) => {
      const tmxExport = state.tmxExport
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          showModal: action.payload
        }
      }
    },
    [GET_LOCALE_SUCCESS]: (state, action) => {
      const tmxExport = state.tmxExport
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          sourceLanguages: action.payload
        }
      }
    },
    [GET_LOCALE_FAILURE]: (state, action) => {
      const tmxExport = state.tmxExport
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          sourceLanguages: []
        }
      }
    },
    [GET_TMX_REQUEST]: (state, action) => {
      const tmxExport = state.tmxExport
      const downloading = cloneDeep(tmxExport.downloading)
      downloading[action.payload.srcLocaleId] = true
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          downloading: downloading
        }
      }
    },
    [GET_TMX_SUCCESS]: (state, action) => {
      const tmxExport = state.tmxExport
      const downloading = cloneDeep(tmxExport.downloading)
      const {blob, srcLocaleId, project, version} = action.payload
      const filename = buildTMXFileName(project, version, srcLocaleId,
        undefined)
      saveAs(blob, filename)
      delete downloading[srcLocaleId]
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          downloading: downloading
        }
      }
    },
    [GET_TMX_FAILURE]: (state, action) => {
      const tmxExport = state.tmxExport
      const downloading = cloneDeep(tmxExport.downloading)
      delete downloading[action.payload.srcLocaleId]
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          downloading: downloading
        }
      }
    }
  },
    // default state
  {
    tmxExport: {
      sourceLocale: undefined,
      targetLocale: undefined,
      showModal: false,
      downloading: {},
      sourceLanguages: undefined,
      type: TMX_TYPE[0]
    }
  }
)

export default tmx

/**
 * reducer for TMX components
 */
import {handleActions} from 'redux-actions'
import {saveAs} from 'file-saver'
import {cloneDeep, isUndefined} from 'lodash'
import update from 'immutability-helper'

import {
  GET_LOCALE_FAILURE,
  GET_LOCALE_SUCCESS,
  GET_TMX_FAILURE,
  GET_TMX_REQUEST,
  GET_TMX_SUCCESS,
  SET_INITIAL_STATE,
  SHOW_EXPORT_TMX_MODAL,
  TMX_ALL
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
      return update(
          state, {
            tmxExport: {
              type: {$set: action.payload}
            }
          }
      )
    },
    [SHOW_EXPORT_TMX_MODAL]: (state, action) => {
      return update(
          state, {
            tmxExport: {
              showModal: {$set: action.payload}
            }
          }
      )
    },
    [GET_LOCALE_SUCCESS]: (state, action) => {
      return update(
          state, {
            tmxExport: {
              sourceLanguages: {$set: action.payload}
            }
          }
      )
    },
    [GET_LOCALE_FAILURE]: (state, action) => {
      return update(
          state, {
            tmxExport: {
              sourceLanguages: {$set: []}
            }
          }
      )
    },
    [GET_TMX_REQUEST]: (state, action) => {
      const downloading = cloneDeep(state.tmxExport.downloading)
      downloading[action.payload.srcLocaleId] = true
      return update(
          state, {
            tmxExport: {
              downloading: {$set: downloading}
            }
          }
      )
    },
    [GET_TMX_SUCCESS]: (state, action) => {
      const downloading = cloneDeep(state.tmxExport.downloading)
      const {blob, srcLocaleId, project, version} = action.payload
      const filename = buildTMXFileName(project, version, srcLocaleId,
          undefined)
      saveAs(blob, filename)
      delete downloading[srcLocaleId]
      return update(
          state, {
            tmxExport: {
              downloading: {$set: downloading}
            }
          }
      )
    },
    [GET_TMX_FAILURE]: (state, action) => {
      const downloading = cloneDeep(state.tmxExport.downloading)
      delete downloading[action.payload.srcLocaleId]
      return update(
          state, {
            tmxExport: {
              downloading: {$set: downloading}
            }
          }
      )
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
      type: TMX_ALL
    }
  }
)

export default tmx

/**
 * reducer for TMX components
 */
import { handleActions } from 'redux-actions'

import {
  TMX_TYPE,
  SHOW_EXPORT_TMX_MODAL,
  TOGGLE_SHOW_SOURCE_LANGUAGES,
  GET_LOCALE_SUCCESS,
  GET_LOCALE_FAILURE,
  SET_INITIAL_STATE
} from '../actions/tmx-actions'

const tmx = handleActions(
  {
    [SET_INITIAL_STATE]: (state, action) => {
      const tmxExport = state.tmxExport
      return {
        ...state,
        project: action.payload.project,
        version: action.payload.version,
        tmxExport: {
          ...tmxExport,
          type: action.payload.type
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
    [TOGGLE_SHOW_SOURCE_LANGUAGES]: (state, action) => {
      const tmxExport = state.tmxExport
      return {
        ...state,
        tmxExport: {
          ...tmxExport,
          showSourceLanguages: !tmxExport.showSourceLanguages
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
    }
  },
    // default state
  {
    project: undefined,
    version: undefined,
    tmxExport: {
      sourceLocale: undefined,
      targetLocale: undefined,
      showModal: true,
      showSourceLanguages: false,
      sourceLanguages: undefined,
      type: TMX_TYPE[0]
    }
  }
)

export default tmx

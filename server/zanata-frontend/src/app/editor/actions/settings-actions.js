// @ts-nocheck
import { CALL_API_ENHANCED } from '../middlewares/call-api'
import { createAction } from 'redux-actions'
import {
  SETTINGS_REQUEST,
  SETTINGS_SUCCESS,
  SETTINGS_FAILURE,
  SETTING_UPDATE,
  SETTINGS_SAVE_REQUEST,
  SETTINGS_SAVE_SUCCESS,
  SETTINGS_SAVE_FAILURE,
  VALIDATORS_REQUEST,
  VALIDATORS_SUCCESS,
  VALIDATORS_FAILURE
} from './settings-action-types'
import { apiUrl } from '../../config'
import { isEmptyObject } from '../../utils/ObjectUtils'

export const settingsUrl = `${apiUrl}/user/settings/webeditor`

/**
 * Fetch the editor settings over the REST API.
 *
 * Note: these settings could be included in the HTML page to avoid a request
 */
export const fetchSettings = () => dispatch => dispatch({
  [CALL_API_ENHANCED]: {
    endpoint: settingsUrl,
    types: [
      SETTINGS_REQUEST,
      SETTINGS_SUCCESS,
      SETTINGS_FAILURE
    ]
  }
})

/**
 * Fetch the Project Validation settings over the REST API.
 */
export function fetchValidationSettings (dispatch, projectSlug, versionSlug) {
  const validationSettingsUrl =
    `${apiUrl}/project/validators/${projectSlug}?versionSlug=${versionSlug}`
  dispatch({
    [CALL_API_ENHANCED]: {
      endpoint: validationSettingsUrl,
      types: [
        VALIDATORS_REQUEST,
        VALIDATORS_SUCCESS,
        VALIDATORS_FAILURE
      ]
    }
  })
}

/**
 * Save one or more settings to the server
 * settings: object of setting name to value
 */
export const saveSettings = settings => dispatch => new Promise(
  (resolve, reject) => {
    if (isEmptyObject(settings)) {
      // no settings to save, something must have gone wrong
      const error = new Error('trying to save empty settings object')
      console.error(error)
      return reject(error)
    }
    dispatch({
      [CALL_API_ENHANCED]: {
        endpoint: settingsUrl,
        method: 'POST',
        body: JSON.stringify(settings),
        types: [
          {
            type: SETTINGS_SAVE_REQUEST,
            meta: { settings }
          },
          {
            type: SETTINGS_SAVE_SUCCESS,
            meta: { settings }
          },
          {
            type: SETTINGS_SAVE_FAILURE,
            meta: { settings }
          }
        ]
      }
    })
    resolve()
  })

/*
 * Update a setting locally and persist it to the server.
 */
export const updateSetting = (key, value) => dispatch => {
  const setting = { [key]: value }
  dispatch(createAction(SETTING_UPDATE)(setting))
  return saveSettings(setting)(dispatch)
}

/*
 * Update a setting locally for the current editor session.
 */
export const updateValidationSetting = (key, value) => dispatch => {
  const setting = { [key]: value }
  dispatch(createAction(SETTING_UPDATE)(setting))
}

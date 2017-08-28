import { CALL_API_ENHANCED } from '../middlewares/call-api'
import {
  SETTINGS_REQUEST,
  SETTINGS_SUCCESS,
  SETTINGS_FAILURE
} from './settings-action-types'
import { baseRestUrl } from '../api'

const settingsUrl = `${baseRestUrl}/user/settings/foo`

/**
 * Fetch the editor settings over the REST API.
 *
 * Note: these settings could be included in the HTML page to avoid a request
 */
export function fetchSettings () {
  return (dispatch, getState) => {
    // FIXME /foo is just to test that this can work
    dispatch({
      [CALL_API_ENHANCED]: {
        endpoint: settingsUrl,
        types: [
          SETTINGS_REQUEST,
          SETTINGS_SUCCESS,
          SETTINGS_FAILURE
        ]
      }
    })
  }
}

/**
 * settings: object of setting name to value
 */
export function saveSettings (settings) {
  return (dispatch, getState) => {
    // FIXME /foo is just to test that this can work
    // FIXME do not dispatch for empty settings
    dispatch({
      [CALL_API_ENHANCED]: {
        endpoint: settingsUrl,
        method: 'POST',
        body: JSON.stringify(settings),
        types: [
          'SAVE_SETTINGS_REQUEST',
          'SAVE_SETTINGS_SUCCESS',
          'SAVE_SETTINGS_FAILURE'
        ]
      }
    })
  }
}

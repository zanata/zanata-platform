import { includes, forEach } from 'lodash'
import { CALL_API } from 'redux-api-middleware'
import {
    getJsonHeaders,
    buildAPIRequest,
    APITypes
} from './common-actions'
import { apiUrl } from '../config'
import {SettingsState} from "../containers/Admin/ServerSettings"

export const GET_SERVER_SETTINGS_FAILURE = 'GET_SERVER_SETTINGS_FAILURE'
export const GET_SERVER_SETTINGS_REQUEST = 'GET_SERVER_SETTINGS_REQUEST'
export const GET_SERVER_SETTINGS_SUCCESS = 'GET_SERVER_SETTINGS_SUCCESS'

export const SAVE_SERVER_SETTINGS_FAILURE = 'SAVE_SERVER_SETTINGS_FAILURE'
export const SAVE_SERVER_SETTINGS_REQUEST = 'SAVE_SERVER_SETTINGS_REQUEST'
export const SAVE_SERVER_SETTINGS_SUCCESS = 'SAVE_SERVER_SETTINGS_SUCCESS'

export function fetchServerSettings () {
  const endpoint = `${apiUrl}/admin/server-settings`
  const apiTypes: APITypes = [
    GET_SERVER_SETTINGS_REQUEST,
    {
        type: GET_SERVER_SETTINGS_SUCCESS,
        payload: (_action: string, _state: any, res: Response) => {
          const contentType = res.headers.get('Content-Type')
          if (contentType && includes(contentType, 'json')) {
            return res.json().then((json) => {
              return json
            })
          }
        },
        meta: {
            receivedAt: Date.now()
        }
    },
    GET_SERVER_SETTINGS_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'GET', getJsonHeaders(), apiTypes)
  }
}

interface KeyValue {
  [key: string]: string | number | boolean | undefined
}

export function handleSaveServerSettings(prevSettings: SettingsState, newSettings: SettingsState) {
  const endpoint = `${apiUrl}/admin/server-settings`

  const updatedConfig: KeyValue = {}
  forEach(newSettings, (prop, key) => {
    if (prevSettings[key].value !== prop.value) {
      updatedConfig[key] = prop.value
    }
  })
  const apiTypes: APITypes = [
    SAVE_SERVER_SETTINGS_REQUEST,
    {
      type: SAVE_SERVER_SETTINGS_SUCCESS,
      payload: (_action: string, _state: any, res: Response) => {
        const contentType = res.headers.get('Content-Type')
        if (contentType && includes(contentType, 'json')) {
          return res.json().then((json) => {
            return json
          })
        }
      },
      meta: {
        receivedAt: Date.now()
      }
    },
    SAVE_SERVER_SETTINGS_FAILURE]
  return {
    [CALL_API]: buildAPIRequest(endpoint, 'POST', getJsonHeaders(), apiTypes,
        JSON.stringify(updatedConfig))
  }
}

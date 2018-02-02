/* global jest describe it expect */

import { CALL_API_ENHANCED } from '../middlewares/call-api'

// import reducer, { KEY_SUGGESTIONS_VISIBLE } from './settings-reducer'
import {
  settingsUrl,
  fetchSettings,
  updateSetting,
  saveSettings
} from './settings-actions'
// import { createAction } from 'typesafe-actions'
import {
  SETTINGS_REQUEST,
  SETTINGS_SUCCESS,
  SETTINGS_FAILURE,
  SETTING_UPDATE,
  SETTINGS_SAVE_REQUEST,
  SETTINGS_SAVE_SUCCESS,
  SETTINGS_SAVE_FAILURE
} from '../actions/settings-action-types'

describe('settings-actions', () => {
  it('fetchSettings generates API call', () => {
    const dispatch = jest.fn()
    fetchSettings()(dispatch)
    expect(dispatch).toBeCalledWith({
      [CALL_API_ENHANCED]: {
        endpoint: settingsUrl,
        types: [
          SETTINGS_REQUEST,
          SETTINGS_SUCCESS,
          SETTINGS_FAILURE
        ]
      }
    })
  })
  it('updateSetting dispatches update and API call', () => {
    const dispatch = jest.fn()
    updateSetting('foo', 'bar')(dispatch).then(() => {
      const settings = { foo: 'bar' }

      // local update
      expect(dispatch).toBeCalledWith({
        type: SETTING_UPDATE,
        payload: settings
      })
      // api request to save settings
      expect(dispatch).toBeCalledWith({
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
    })
  })
  it('saveSettings does not dispatch empty settings', () => {
    console.error = jest.fn()
    const dispatch = jest.fn()
    expect(saveSettings({})(dispatch)).rejects.toBeDefined()
    expect(console.error).toBeCalled()
  })
})

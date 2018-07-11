import {handleActions} from 'redux-actions'
import { keyBy } from 'lodash'
import update from 'immutability-helper'
import {
  GET_ALL_CRITERIA_SUCCESS,
  ADD_CRITERION_SUCCESS,
  EDIT_CRITERION_SUCCESS,
  DELETE_CRITERION_SUCCESS,
  ADD_CRITERION_FAILURE,
  EDIT_CRITERION_FAILURE,
  DELETE_CRITERION_FAILURE,
  GET_ALL_CRITERIA_FAILURE
} from '../actions/review-actions'

import {
  GET_SERVER_SETTINGS_FAILURE,
  GET_SERVER_SETTINGS_SUCCESS,
  GET_SERVER_SETTINGS_REQUEST,
  SAVE_SERVER_SETTINGS_FAILURE,
  SAVE_SERVER_SETTINGS_SUCCESS,
  SAVE_SERVER_SETTINGS_REQUEST
} from '../actions/admin-actions'

import { SEVERITY } from '../actions/common-actions'

/** @disabled.type {import('./state').AdminState} */
const defaultState = {
  notification: undefined,
  review: {
    key: undefined,
    criteria: []
  },
  serverSettings: {
    key: undefined,
    loading: false,
    saving: false,
    settings: {}
  }
}

const SAVE_SERVER_SETTINGS_FAILED_MSG = 'Failed to save settings.' +
    'Please refresh this page and try again'

const GET_SERVER_SETTINGS_FAILED_MSG = 'Unable to get server settings.' +
    'Please refresh this page and try again'

const SAVE_SERVER_SETTINGS_MSG = 'Server settings updated'

// selectors
// @ts-ignore any
const getCriteria = state => state.review.criteria

export const selectors = {
  getCriteria,
  // @ts-ignore any
  getNotification: state => state.notification
}

// utility function
// @ts-ignore any
const getErrorMessage = action => {
  if (action.error) {
    return action.payload && action.payload.message
  }
  return undefined
}

const admin = handleActions({
  [GET_SERVER_SETTINGS_REQUEST]: (state) => {
    return update(state, {
      serverSettings: { loading: { $set: true } }
    })
  },
  [SAVE_SERVER_SETTINGS_REQUEST]: (state) => {
    return update(state, {
      serverSettings: { saving: { $set: true } }
    })
  },
  [GET_SERVER_SETTINGS_SUCCESS]: (state, action) => {
    if (action.error) {
      return update(state, {
        serverSettings: { loading: { $set: false } },
        notification: {
          $set: {
            severity: SEVERITY.ERROR,
            message: GET_SERVER_SETTINGS_FAILED_MSG,
            description: getErrorMessage(action)
          }
        }
      })
    } else {
      return update(state, {
        notification: { $set: undefined },
        serverSettings: {
          loading: { $set: false },
          settings: { $set: keyBy(action.payload, o => o.key) }
        }
      })
    }
  },
  [SAVE_SERVER_SETTINGS_SUCCESS]: (state, action) => {
    if (action.error) {
      return update(state, {
        serverSettings: { saving: { $set: false } },
        notification: {
          $set: {
            severity: SEVERITY.ERROR,
            message: SAVE_SERVER_SETTINGS_FAILED_MSG,
            description: getErrorMessage(action)
          }
        }
      })
    } else {
      return update(state, {
        notification: {
          $set: {
            severity: SEVERITY.INFO,
            message: SAVE_SERVER_SETTINGS_MSG
          }
        },
        serverSettings: {
          saving: { $set: false },
          settings: { $set: keyBy(action.payload, o => o.key) }
        }
      })
    }
  },
  [GET_ALL_CRITERIA_SUCCESS]: (state, action) => {
    return update(state, {
      review: { criteria: { $set: action.payload } }
    })
  },
  [ADD_CRITERION_SUCCESS]: (state, action) => {
    return update(state, {
      review: { criteria: { $push: [action.payload] } }
    })
  },
  [EDIT_CRITERION_SUCCESS]: (state, action) => {
    const index = state.review.criteria
      // @ts-ignore
      .findIndex(c => c.id === action.payload.id)
    if (index >= 0) {
      return update(state, {
        review: { criteria: { [index]: { $set: action.payload } } }
      })
    }
    return state
  },
  [DELETE_CRITERION_SUCCESS]: (state, action) => {
    const index = state.review.criteria
      // @ts-ignore
      .findIndex(c => c.id === action.payload.id)
    if (index >= 0) {
      return update(state, {
        review: { criteria: { $splice: [[index, 1]] } }
      })
    }
    return state
  },
  [GET_SERVER_SETTINGS_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: GET_SERVER_SETTINGS_FAILED_MSG,
          description: getErrorMessage(action)
        }
      },
      serverSettings: {
        loading: { $set: false },
        settings: { }
      }
    })
  },
  [ADD_CRITERION_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Add Criteria failed.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [EDIT_CRITERION_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Edit criteria failed.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [DELETE_CRITERION_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Delete Criteria failed.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [GET_ALL_CRITERIA_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: `Failed to retrieve review criteria.`,
          description: getErrorMessage(action)
        }
      }
    })
  },
  [SAVE_SERVER_SETTINGS_FAILURE]: (state, action) => {
    return update(state, {
      notification: {
        $set: {
          severity: SEVERITY.ERROR,
          message: SAVE_SERVER_SETTINGS_FAILED_MSG,
          description: getErrorMessage(action)
        }
      },
      serverSettings: {
        saving: {$set: false}
      }
    })
  }
}, defaultState)

export default admin

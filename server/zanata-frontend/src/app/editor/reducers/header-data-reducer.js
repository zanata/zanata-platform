// TODO refactor all this state to a sensible structure
// e.g. 'user' and 'context' can just be top-level items
import { handleActions } from 'redux-actions'
import {
  DOCUMENT_SELECTED,
  HEADER_DATA_FETCHED,
  LOCALE_SELECTED,
  STATS_FETCHED,
  USER_PERMISSION_SUCCESS,
  LOCALE_MESSAGES_SUCCESS
} from '../actions/header-action-types'
import update from 'immutability-helper'
import {prepareLocales, prepareStats, prepareDocs} from '../utils/Util'
import { dashboardUrl, projectPageUrl } from '../api'

const defaultState = {
  user: {
    name: '',
    gravatarUrl: '',
    dashboardUrl: ''
  },
  context: {
    projectVersion: {
      project: {
        slug: '',
        name: ''
      },
      version: '',
      url: '',
      docs: [],
      locales: {}
    },
    selectedDoc: {
      counts: {
        total: 0,
        approved: 0,
        rejected: 0,
        translated: 0,
        needswork: 0,
        untranslated: 0
      },
      id: ''
    },
    selectedLocale: '',
    localeMessages: {}
  },
  permissions: {
    reviewer: false,
    translator: false
  }
}

const gravatarUrl = (hash, size) => {
  return `https://www.gravatar.com/avatar/${hash}?d=mm&r=g&s=${size}`
}

const headerDataReducer = handleActions({
  [HEADER_DATA_FETCHED]: (state, { payload: {
    documents, locales, versionSlug, projectInfo, myInfo } }) => {
    const projectSlug = projectInfo.id
    return update(state, {
      user: {
        name: {$set: myInfo.name},
        // FIXME server is providing myInfo.imageUrl not gravatarHash
        gravatarUrl: {$set: gravatarUrl(myInfo.gravatarHash, 72)},
        dashboardUrl: { $set: dashboardUrl }
      },
      context: {
        projectVersion: {
          project: {
            slug: {$set: projectSlug},
            name: {$set: projectInfo.name}
          },
          version: {$set: versionSlug},
          url: {$set: projectPageUrl(projectSlug, versionSlug)},
          docs: {$set: prepareDocs(documents)},
          locales: {$set: prepareLocales(locales)}
        }
      }
    })
  },

  [DOCUMENT_SELECTED]: (state, { payload }) =>
    update(state, { context: { selectedDoc: { id: {$set: payload} } } }),

  [LOCALE_SELECTED]: (state, { payload }) =>
    update(state, { context: { selectedLocale: {$set: payload} } }),

  [LOCALE_MESSAGES_SUCCESS]: (state, { payload }) =>
    update(state, { localeMessages: {$set: payload} }),

  [STATS_FETCHED]: (state, { payload }) => update(state, {
    context: { selectedDoc: { counts: {$set: prepareStats(payload)} } } }),

  [USER_PERMISSION_SUCCESS]: (state, { payload }) => update(state, {
    permissions: {$set: payload} })
}, defaultState)

export default headerDataReducer

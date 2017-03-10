import {
  DOCUMENT_SELECTED,
  HEADER_DATA_FETCHED,
  LOCALE_SELECTED,
  STATS_FETCHED
} from '../actions/headerActions'
import updateObject from 'react-addons-update'
import {prepareLocales, prepareStats, prepareDocs} from '../utils/Util'
import { dashboardUrl, serviceUrl } from '../api'

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
    selectedLocale: ''
  }
}

const projectPage = (projectSlug, versionSlug) => {
  return `${serviceUrl}/iteration/view/${projectSlug}/${versionSlug}`
}

const gravatarUrl = (hash, size) => {
  return `http://www.gravatar.com/avatar/${hash}?d=mm&ampr=g&amps=${size}`
}

export default (state = defaultState, action) => {
  switch (action.type) {
    case HEADER_DATA_FETCHED:
      const docs = prepareDocs(action.data.documents)
      const locales = prepareLocales(action.data.locales)
      const versionSlug = action.data.versionSlug
      const projectSlug = action.data.projectInfo.id
      const projectName = action.data.projectInfo.name
      const name = action.data.myInfo.name
      const gravatarHash = action.data.myInfo.gravatarHash

      return updateObject(state, {
        user: {
          name: {
            $set: name
          },
          gravatarUrl: {
            $set: gravatarUrl(gravatarHash, 72)
          },
          dashboardUrl: {
            $set: dashboardUrl
          }

        },
        context: {
          projectVersion: {
            project: {
              slug: {
                $set: projectSlug
              },
              name: {
                $set: projectName
              }
            },
            version: {
              $set: versionSlug
            },
            url: {
              $set: projectPage(projectSlug, versionSlug)
            },
            docs: {
              $set: docs
            },
            locales: {
              $set: locales
            }
          }
        }
      })

    case DOCUMENT_SELECTED:

      return updateObject(state, {
        context: {
          selectedDoc: {
            id: {
              $set: action.data.selectedDocId
            }
          }
        }
      })

    case LOCALE_SELECTED:
      return updateObject(state, {
        context: {
          selectedLocale: {
            $set: action.data.selectedLocaleId
          }
        }
      })

    case STATS_FETCHED:
      const counts = prepareStats(action.data)
      return updateObject(state, {
        context: {
          selectedDoc: {
            counts: {
              $set: counts
            }
          }
        }
      })

    default:
      return state
  }
}

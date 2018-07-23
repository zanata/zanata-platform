/* React to changes that require a new flyweight list of phrases to be fetched.
 *
 * Includes filtered and unfiltered phrases lists.
 */

import { createSelector } from 'reselect'
import watch from './watch'
import { debounce, every, isEmpty } from 'lodash'
import { getLang } from '../selectors'
import { getJSON } from 'redux-api-middleware'
import { CALL_API_ENHANCED } from '../middlewares/call-api'
import { encode } from '../utils/doc-id-util'
import { apiUrl } from '../../config'
import { transUnitStatusToPhraseStatus } from '../utils/status-util'
import { hasAdvancedFilter } from '../utils/filter-util'
import {
  PHRASE_LIST_REQUEST,
  PHRASE_LIST_SUCCESS,
  PHRASE_LIST_FAILURE
} from '../actions/phrases-action-types'

// @ts-ignore any
const getProject = state => state.context.projectSlug
// @ts-ignore any
const getVersion = state => state.context.versionSlug
// @ts-ignore any
const getDocId = state => state.context.docId
// @ts-ignore any
const getAdvancedFilter = state => state.phrases.filter.advanced

const getPhraseListInfo = createSelector(
  getProject, getVersion, getLang, getDocId,
  // Just put them all in an object to use when it changes
  (project, version, lang, docId) => ({ project, version, lang, docId })
)

const getFilterPhraseListInfo = createSelector(
  getPhraseListInfo, getAdvancedFilter,
  (phraseListInfo, advancedFilter) => ({ ...phraseListInfo, advancedFilter })
)

/* Watch for changes that mean a new phrase list is needed.
 *
 * TODO (optimization) only run if phrases.inDoc[docId] is not set yet
 *                     or is stale.
 */
// @ts-ignore any
export function watchRequiredPhraseList (store) {
  const watcher = watch('watchRequiredPhraseList')(
    () => getPhraseListInfo(store.getState()))
  const debounceCallApi = debounce(
    (project, version, lang, docId) => {
      // dispatch included within debounce to avoid repeated dispatch of the
      // same API call action.
      store.dispatch(
        fetchPhraseList(project, version, lang, docId))
    }, 1000)

  store.subscribe(watcher(
    // @ts-ignore any
    ({ project, version, lang, docId }, _prevState) => {
      if (isEmpty(project) || isEmpty(version) ||
        isEmpty(lang) || isEmpty(docId)) {
        return
      }
      debounceCallApi(project, version, lang, docId)
    }))
}

// @ts-ignore any
export const watchAdvancedFilterList = (store) => {
  const watcher = watch('watchAdvancedFilterList')(
    () => getFilterPhraseListInfo(store.getState()))
  const debounceCallApi = debounce(
    (project, version, lang, docId, advancedFilter) => {
      // dispatch included within debounce to avoid repeated dispatch of the
      // same API call action.
      store.dispatch(
        fetchPhraseList(project, version, lang, docId, advancedFilter))
    }, 1000)

  store.subscribe(watcher(
    // @ts-ignore any
    ({ project, version, lang, docId, advancedFilter }) => {
      if (isEmpty(project) || isEmpty(version) ||
        isEmpty(lang) || isEmpty(docId)) {
        // not enough info to fetch yet
        return
      }
      if (!every(advancedFilter, isEmpty)) {
        debounceCallApi(project, version, lang, docId, advancedFilter)
      }
    }))
}

// @ts-ignore any
function fetchPhraseList (project, version, localeId, docId, filter) {
  const filtered = !!filter
  if (filtered && !hasAdvancedFilter(filter)) {
    // empty filter, just let it fall back on unfiltered list
    return
  }
  const encodedId = encode(docId)
  const url =
    `${apiUrl}/project/${project}/version/${version}/doc/${encodedId}/status/${localeId}` // eslint-disable-line max-len

  return {
    [CALL_API_ENHANCED]: {
      endpoint: url,
      method: 'POST',
      body: JSON.stringify(filter || {}),
      types: [
        {
          type: PHRASE_LIST_REQUEST,
          meta: { filter: filtered }
        },
        {
          type: PHRASE_LIST_SUCCESS,
          // @ts-ignore any
          payload: (_action, _state, res) =>
            // @ts-ignore
            getJSON(res).then(phraseList => ({
              docId,
              // @ts-ignore any
              phraseList: phraseList.map(phrase => ({
                ...phrase,
                status: transUnitStatusToPhraseStatus(phrase.status)
              }))
            })),
          meta: { filter: filtered }
        },
        {
          type: PHRASE_LIST_FAILURE,
          meta: { filter: filtered }
        }
      ]
    }
  }
}

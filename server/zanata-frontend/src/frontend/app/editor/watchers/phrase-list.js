/* React to changes that require a new flyweight list of phrases to be fetched.
 *
 * Includes filtered and unfiltered phrases lists.
 */

import { createSelector } from 'reselect'
import watch from './watch'
import { debounce, every, isEmpty } from 'lodash'
// import { fetchPhraseList } from '../api'
import { getLang } from '../selectors'
import { CALL_API, getJSON } from 'redux-api-middleware'
import { getJsonWithCredentials } from '../utils/api-util'
import { encode } from '../utils/doc-id-util'
import { baseRestUrl, filterQueryString } from '../api'
import { transUnitStatusToPhraseStatus } from '../utils/status-util'
import {
  PHRASE_LIST_REQUEST,
  PHRASE_LIST_SUCCESS,
  PHRASE_LIST_FAILURE
} from '../actions/phrases-action-types'

const getProject = state => state.context.projectSlug
const getVersion = state => state.context.versionSlug
const getDocId = state => state.context.docId

// Tradeoff: for max flexibility, could make a selector for every level,
// so there would be less to change when something moves (just change one
// selector). It means moderately deep selectors are less easy to match up to
// what part of state they are talking about.
// Note also: if selectors were packaged in/with reducers, it would keep the
// state shape information all in one place. Perhaps phraseReducer.getFoo
// would be a good place to store selectors.
// import { getFoo, getBar } from '../reducers/phraseReducer'
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

// If any of those things change, we need to fetch a new thing
// but better debounce it to avoid thrashing.
// This returns a promise, but it would return it multiple times so I
// can't just do .then() on it since I'll attach so many things.
// Need to make a function with the whole lot in it that I can debounce
// DO IT because this will replace the standard one soon anyway.

/* Watch for changes that mean a new phrase list is needed.
 *
 * TODO (optimization) only run if phrases.inDoc[docId] is not set yet
 *                     or is stale.
 */
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
    ({ project, version, lang, docId }, prevState) => {
      if (isEmpty(project) || isEmpty(version) ||
        isEmpty(lang) || isEmpty(docId)) {
        return
      }
      debounceCallApi(project, version, lang, docId)
    }))
}

export const watchAdvancedFilterList = (store) => {
  // FIXME use an appropriate comparator.
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

function fetchPhraseList (project, version, localeId, docId, filter) {
  // TODO short-circuit empty-filter to immediately dispatch empty filter list
  const hasFilter = !!filter
  const encodedId = encode(docId)
  const queryString = hasFilter ? filterQueryString(filter) : ''
  const url =
    `${baseRestUrl}/project/${project}/version/${version}/doc/${encodedId}/status/${localeId}?${queryString}` // eslint-disable-line max-len

  return {
    [CALL_API]: getJsonWithCredentials({
      endpoint: url,
      types: [
        {
          type: PHRASE_LIST_REQUEST,
          meta: { filter: hasFilter }
        },
        {
          type: PHRASE_LIST_SUCCESS,
          payload: (action, state, res) =>
            getJSON(res).then(phraseList => ({
              docId,
              phraseList: phraseList.map(phrase => ({
                ...phrase,
                status: transUnitStatusToPhraseStatus(phrase.status)
              }))
            })),
          meta: { filter: hasFilter }
        },
        {
          type: PHRASE_LIST_FAILURE,
          meta: { filter: hasFilter }
        }
      ]
    })
  }
}

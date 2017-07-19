/* React to changes that could change filtered phrases (list) and visible
 * filtered phrases.
 *
 * This should expand to handle all phrase fetching. It does not yet because
 * new-context-fetch is handling some of it.
 */

import { createSelector } from 'reselect'
import watch from 'redux-watch'
import { debounce, every, isEmpty } from 'lodash'
// import { fetchPhraseList } from '../api'
import { getLang } from '../selectors'
import { CALL_API, getJSON } from 'redux-api-middleware'
import { getJsonWithCredentials } from '../utils/api-util'
import { encode } from '../utils/doc-id-util'
import { baseRestUrl, filterQueryString } from '../api'
import { transUnitStatusToPhraseStatus } from '../actions/phrases-actions'

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
export const watchRequiredPhraseList = (store) => {
  const watcher = watch(() => getPhraseListInfo(store.getState()))
  const debounceCallApi = debounce(
    (project, version, lang, docId) => {
      // dispatch included within debounce to avoid repeated dispatch of the
      // same API call action.
      store.dispatch(
        fetchPhraseList(project, version, lang, docId))
    }, 1000)

  store.subscribe(watcher(
    ({ project, version, lang, docId }) => {
      console.log('Phrase list watcher triggered')
      if (isEmpty(project) || isEmpty(version) ||
        isEmpty(lang) || isEmpty(docId)) {
        // not enough info to fetch yet
        console.log('Phrase list: Still waiting on some context data',
          project, version, lang, docId)
        return
      }

      console.log('Phrase list: running debounce API call')
      debounceCallApi(project, version, lang, docId)
    }))
}

export const watchAdvancedFilterList = (store) => {
  const watcher = watch(() => getFilterPhraseListInfo(store.getState()))
  const debounceCallApi = debounce(
    (project, version, lang, docId, advancedFilter) => {
      // dispatch included within debounce to avoid repeated dispatch of the
      // same API call action.
      store.dispatch(
        fetchPhraseList(project, version, lang, docId, advancedFilter))
    }, 1000)

  store.subscribe(watcher(
    ({ project, version, lang, docId, advancedFilter }) => {
      console.log('Advanced filter list watcher triggered')
      if (isEmpty(project) || isEmpty(version) ||
        isEmpty(lang) || isEmpty(docId)) {
        // not enough info to fetch yet
        console.log('Advanced filter list: Still waiting on some context data',
          project, version, lang, docId)
        return
      }
      if (every(advancedFilter, isEmpty)) {
        console.log('Advanced filter list: no filter set, nothing to do.')
      } else {
        console.log('Advanced filter list: running debounce API call')
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

  // TODO also handle filter: false when filter is not specified

  return {
    [CALL_API]: getJsonWithCredentials({
      endpoint: url,
      types: [
        // TODO use these types for both filter and non-filter,
        //      use meta.filter to distinguish in reducer
        {
          type: 'PHRASE_LIST_REQUEST',
          meta: { filter: hasFilter }
        },
        {
          type: 'PHRASE_LIST_SUCCESS',
          payload: (action, state, res) =>
            getJSON(res).then(statusList => ({
              docId,
              statusList: statusList.map(phrase => ({
                ...phrase,
                status: transUnitStatusToPhraseStatus(phrase.status)
              }))
            })),
          meta: { filter: hasFilter }
        },
        {
          type: 'PHRASE_LIST_FAILED',
          meta: { filter: hasFilter }
        }
      ]
    })
  }
}

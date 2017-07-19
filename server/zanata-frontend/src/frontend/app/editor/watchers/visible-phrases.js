/* Fetch phrase detail when phrases become visible.
 *
 * Some examples:
 *  - new document opened
 *  - user moved to next page
 *  - user changed page size
 *  - user filtered phrase list
 */

import watch from 'redux-watch'
import { fetchPhraseDetail } from '../api'
import { getCurrentPagePhrasesAndLang } from '../selectors'

export const watchVisiblePhrasesInStore = (store) => {
  const watcher = watch(() => getCurrentPagePhrasesAndLang(store.getState()))
  store.subscribe(watcher(({ phrases, lang }/*, oldVal*/) => {
    fetchPhraseDetail(lang, phrases)
      // TODO needs transforming and stuff
      .then()
    store.dispatch(/* CALL_API action creator instead */)
  }))
}

// This should only be used when there is a cache policy for the detail.
// At the moment it is much safer to replace all detail since some could
// be stale. This will work when detail is timestamped to include stale
// detail. Also need a staleness check to happen periodically, or set up
// on a timer based on current oldest.
// export const fetchPhraseDetailWhenNeeded = (store) => {
//   const watcher = watch(() => getMissingPhrases(store.getState()))
//   store.subscribe(watcher((newVal, oldVal) => {
//     // store.dispatch(fetchPhraseDetail(localeId, newVal))
//   }))
// }

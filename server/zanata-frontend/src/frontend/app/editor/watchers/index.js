/*
 * Watchers react to changes in the store state.
 *
 * e.g. to fetch phrase data when a new document or locale is selected.
 */

import {
  watchRequiredPhraseList,
  watchAdvancedFilterList
} from './filtered-phrases'

export default function addWatchers (store) {
  watchRequiredPhraseList(store)
  watchAdvancedFilterList(store)
}

/*
 * Watchers react to changes in the store state.
 *
 * e.g. to fetch phrase data when a new document or locale is selected.
 */

import { watchQueryStringPageNumber } from './page-number'
import { watchVisiblePhrasesInStore } from './phrase-detail'
import {
  watchRequiredPhraseList,
  watchAdvancedFilterList
} from './phrase-list'
import { watchSelectedPhraseSearches } from './selected-phrase-searches'

export default function addWatchers (store) {
  watchRequiredPhraseList(store)
  watchAdvancedFilterList(store)
  watchVisiblePhrasesInStore(store)
  watchQueryStringPageNumber(store)
  watchSelectedPhraseSearches(store)
}

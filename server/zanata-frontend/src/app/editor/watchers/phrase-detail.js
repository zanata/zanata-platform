/* Fetch phrase detail when phrases become visible.
 *
 * Some examples:
 *  - new document opened
 *  - user moved to next page
 *  - user changed page size
 *  - user filtered phrase list
 */

import watch from './watch'
import { getPhraseDetailFetchData } from '../selectors'
import { apiUrl } from '../../config'
import { fill, isEmpty, mapValues } from 'lodash'
import { getJSON } from 'redux-api-middleware'
import { CALL_API_ENHANCED } from '../middlewares/call-api'
import {
  STATUS_UNTRANSLATED,
  transUnitStatusToPhraseStatus
} from '../utils/status-util'
import {
  PHRASE_DETAIL_REQUEST,
  PHRASE_DETAIL_SUCCESS,
  PHRASE_DETAIL_FAILURE
} from '../actions/phrases-action-types'

export const watchVisiblePhrasesInStore = (store) => {
  const watcher = watch('phrase-detail > watchVisiblePhrasesInStore')(
    () => getPhraseDetailFetchData(store.getState()))
  store.subscribe(watcher(({ phrases, locale, fetching }) => {
    // Only want one detail request at a time.
    // Watcher triggers again when this flips to false.
    if (fetching) {
      return
    }

    const ids = phrases.map(phrase => phrase.id)
    if (locale && !isEmpty(ids)) {
      // TODO debounce to handle rapid page changes etc?
      store.dispatch(fetchPhraseDetail(locale, ids))
    }
  }))
}

function fetchPhraseDetail (locale, phraseIds) {
  const phraseDetailUrl =
    `${apiUrl}/source+trans/${locale.id}?ids=${phraseIds.join(',')}`
  return {
    [CALL_API_ENHANCED]: {
      endpoint: phraseDetailUrl,
      types: [
        PHRASE_DETAIL_REQUEST,
        {
          type: PHRASE_DETAIL_SUCCESS,
          payload: (_action, _state, res) => getJSON(res)
            .then(details => transUnitDetailToPhraseDetail(details, locale))
        },
        PHRASE_DETAIL_FAILURE
      ]
    }
  }
}

/**
 * Convert the TransUnit response objects to the Phrase structure that
 * is needed for the component tree.
 */
function transUnitDetailToPhraseDetail (transUnitDetail, locale) {
  const localeId = locale.id
  const nplurals = locale.nplurals || 1
  return mapValues(transUnitDetail, (transUnit, index) => {
    const {
      content,
      contents,
      id,
      msgctxt,
      plural,
      sourceComment,
      sourceFlags,
      sourceReferences,
      wordCount
    } = transUnit.source
    const trans = transUnit[localeId]
    const status = transUnitStatusToPhraseStatus(trans && trans.state)
    const translations = extractTranslations(trans)
    const emptyTranslations = Array(plural ? nplurals : 1)
    fill(emptyTranslations, '')
    const newTranslations =
        status === STATUS_UNTRANSLATED ? emptyTranslations : [...translations]

    return {
      id: parseInt(index, 10),
      resId: id,
      msgctxt,
      sourceComment,
      sourceFlags,
      sourceReferences,
      plural,
      sources: plural ? contents : [content],
      translations,
      newTranslations,
      status,
      revision: trans && trans.revision ? parseInt(trans.revision, 10) : 0,
      wordCount: parseInt(wordCount, 10),
      lastModifiedBy: trans && trans.translator && trans.translator.name,
      lastModifiedTime: trans && trans.lastModifiedTime &&
        new Date(trans.lastModifiedTime)
    }
  })
}

/**
 * Get translations from a TransUnit in a consistent form (array of strings)
 *
 * This will always return an Array<String>, but the array may be empty.
 */
function extractTranslations (trans) {
  if (!trans) {
    return []
  }
  return trans.content ? [trans.content]
    // Array.slice() efficiently makes a copy of the array.
    : (trans.contents ? trans.contents.slice() : [])
}

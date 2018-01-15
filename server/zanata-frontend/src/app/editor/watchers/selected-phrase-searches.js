import watch from './watch'
import { getSelectedPhrase } from '../selectors'
import { glossarySearchTextEntered } from '../actions/glossary-actions'
import { findPhraseSuggestions } from '../actions/suggestions-actions'

export function watchSelectedPhraseSearches (store) {
  const watcher = watch(
    'selected-phrase-searches > watchSelectedPhraseSearches')(
    () => getSelectedPhrase(store.getState()))

  store.subscribe(watcher(
    (phrase, prevPhrase) => {
      // only want to search if there is a phrase and it has different phrase id
      const newPhrase = phrase && (!prevPhrase || phrase.id !== prevPhrase.id)
      if (newPhrase) {
        store.dispatch(glossarySearchTextEntered(phrase.sources.join(' ')))
        store.dispatch(findPhraseSuggestions(phrase))
      }
    }))
}

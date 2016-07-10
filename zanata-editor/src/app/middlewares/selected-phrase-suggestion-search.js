import stateChangeDispatchMiddleware from './state-change-dispatch'
import { findPhraseSuggestionsById } from '../actions/suggestions'

/**
 * Middleware to search for phrase suggestions when a phrase is selected.
 */
const searchSelectedPhraseMiddleware = stateChangeDispatchMiddleware(
  (dispatch, oldState, newState) => {
    const oldSelectedPhrase = oldState.phrases.selectedPhraseId
    const newSelectedPhrase = newState.phrases.selectedPhraseId
    const phraseSelectionChanged =
      oldSelectedPhrase !== newSelectedPhrase
    if (phraseSelectionChanged) {
      dispatch(findPhraseSuggestionsById(newSelectedPhrase))
    }
  },
)

export default searchSelectedPhraseMiddleware

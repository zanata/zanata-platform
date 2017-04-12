import stateChangeDispatchMiddleware from './state-change-dispatch'
import { findGlossaryTermsByPhraseId } from '../actions/glossary'
import { findPhraseSuggestionsById } from '../actions/suggestions'

/**
 * Middleware to search suggestions and glossary when a phrase is selected.
 */
const searchSelectedPhraseMiddleware = stateChangeDispatchMiddleware(
  (dispatch, oldState, newState) => {
    const oldSelectedPhrase = oldState.phrases.selectedPhraseId
    const newSelectedPhrase = newState.phrases.selectedPhraseId
    const phraseSelectionChanged =
      oldSelectedPhrase !== newSelectedPhrase
    if (phraseSelectionChanged) {
      // TODO both these have to wait until detail is loaded, should handle that
      //      here and call a function with the detail.

      dispatch(findGlossaryTermsByPhraseId(newSelectedPhrase))
      dispatch(findPhraseSuggestionsById(newSelectedPhrase))
    }
  },
)

export default searchSelectedPhraseMiddleware

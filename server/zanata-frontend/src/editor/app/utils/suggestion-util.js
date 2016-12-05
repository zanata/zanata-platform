/**
 * Utility functions for suggestions.
 */

/**
 * Figure out the match type for a suggestion match based on its details.
 *
 * @param suggestionDetail with .type and .contentState
 * @returns String, one of: 'imported', 'translated', 'approved'
 */
export function matchType (suggestionDetail) {
  const { contentState, type } = suggestionDetail
  if (type === 'IMPORTED_TM') {
    return 'imported'
  }
  if (type === 'LOCAL_PROJECT') {
    if (contentState === 'Translated') {
      return 'translated'
    }
    if (contentState === 'Approved') {
      return 'approved'
    }
  }
  console.error('Unrecognised suggestion type or contentState: ',
    JSON.stringify(suggestionDetail))
}

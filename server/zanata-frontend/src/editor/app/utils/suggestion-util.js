/**
 * Utility functions and constants for suggestions.
 */

export const MATCH_TYPE = {
  TRANSLATED: 'translated',
  APPROVED: 'approved',
  IMPORTED: 'imported'
}

/**
 * Figure out the match type for a suggestion match based on its details.
 *
 * @param suggestionDetail with .type and .contentState
 * @returns String, one of: 'imported', 'translated', 'approved'
 */
export function matchType (suggestionDetail) {
  const { contentState, type } = suggestionDetail
  if (type === 'IMPORTED_TM') {
    return MATCH_TYPE.IMPORTED
  }
  if (type === 'LOCAL_PROJECT') {
    if (contentState === 'Translated') {
      return MATCH_TYPE.TRANSLATED
    }
    if (contentState === 'Approved') {
      return MATCH_TYPE.APPROVED
    }
  }
  console.error('Unrecognised suggestion type or contentState: ',
    JSON.stringify(suggestionDetail))
}

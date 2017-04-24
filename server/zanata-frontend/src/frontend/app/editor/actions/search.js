export const TOGGLE_ADVANCED_SEARCH_PANEL =
  Symbol('TOGGLE_ADVANCED_SEARCH_PANEL')
export function toggleAdvancedSearchPanel () {
  return {
    type: TOGGLE_ADVANCED_SEARCH_PANEL
  }
}

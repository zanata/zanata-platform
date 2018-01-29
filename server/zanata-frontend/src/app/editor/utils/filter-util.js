import { some, negate, isEmpty } from 'lodash'

/* Check if there is any search in the advanced filter fields */
export function hasAdvancedFilter (advancedFilter) {
  return some(advancedFilter, negate(isEmpty))
}

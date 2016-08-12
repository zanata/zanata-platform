/**
 * Configuration props used by the Intl mixin.
 */

// default, mainly for use in tests
export const locale = 'en-US'

export const formats = {
  date: {
    medium: {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    }
  }
}

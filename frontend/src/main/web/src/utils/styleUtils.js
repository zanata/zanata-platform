import { merge, values } from 'lodash'
import flattenObj from 'flat'

/**
 * Converts base and otherClass to a simple key/value object.
 *
 * Example :
 * base =
 *  base {
 *   ai: 'Ai(st)',
 *   d: 'D(f)',
 *   fld: 'Fld(c)',
 *   flxs: 'Flxs(0)'
 *  }
 *
 * otherClasses =
 *  base {
 *   flx: 'Flx(flx1)',
 *   flxs: '',
 *   ov: 'Ov(h)'
 *  }
 *
 * results: Ai(st) D(f) Fld(c)  Flx(flx1) Ov(h)
 *
 */
export const flattenThemeClasses = (base, ...otherClasses) => {
  return values(
    flattenObj(merge({}, base, ...otherClasses))
  ).join(' ').trim()
}

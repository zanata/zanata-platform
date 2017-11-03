/* Enhances redux-watch to add logging.
 *
 * Add the watcher name for the logger to use:
 *
 *   original: watch(selector) // plain redux-watch
 *   enhanced: watch('myWatcher')(selector)
 */

import reduxWatch from 'redux-watch'

const withLog = name => f => {
  if (process.env && (process.env.NODE_ENV === 'development')) {
    return (state, prevState) => {
      /* eslint-disable no-console */
      console.group('%c watcher: %s', 'font-weight: bold; color: #CC6600', name)
      console.log('%c was', 'font-weight: bold; color: #9E9E9E', prevState)
      console.log('%c now', 'font-weight: bold; color: #03A9F4', state)
      console.groupEnd()
      /* eslint-enable no-console */
      return f(state, prevState)
    }
  } else {
    return f
  }
}

const watch = (name) => (...watchArgs) => (callback) =>
  reduxWatch(...watchArgs)(withLog(name)(callback))

export default watch

/* Enahanced CALL_API that sets some defaults and gives a better API */

import { CALL_API } from 'redux-api-middleware'

// use this key instead of CALL_API to trigger this middleware
export const CALL_API_ENHANCED = Symbol('CALL_API_ENHANCED')

/*
 * Applies the following enhancements before passing to redux-api-middleware:
 *
 * - defaults method to 'GET' if not specified
 * - always include credentials
 * - sets JSON content type and accept headers
 * - adds meta.timestamp to request, success and failure actions
 */
export default store => next => action => {
  if (action[CALL_API_ENHANCED]) {
    const { headers, types, ...remainingKeys } = action[CALL_API_ENHANCED]
    return next({
      [CALL_API]: {
        // default, overridden if remainingKeys.method is present
        method: 'GET',
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          ...headers
        },
        types: typesWithTimestamp(types),
        // remaining keys are last to allow overriding anything above
        ...remainingKeys
      }
    })
  } else {
    return next(action)
  }
}

/* Add meta.timestamp to all the given methods */
function typesWithTimestamp ([request, success, failure]) {
  const timestamp = Date.now()
  return [
    withTimestamp(request, timestamp),
    withTimestamp(success, timestamp),
    withTimestamp(failure, timestamp)
  ]
}

/* Add meta.timestamp to a redux-api-middleware type descriptor */
// Note: may not work properly when meta is or returns a promise. If we start
// using promises for meta, this should be updated.
function withTimestamp (type, timestamp) {
  const normalType = typeof type === 'string' || typeof type === 'symbol'
    ? { type }
    : type
  const oldMeta = normalType.meta || {}
  const meta = typeof oldMeta === 'function'
    ? (...args) => ({ ...(oldMeta(...args)), timestamp })
    : { ...oldMeta, timestamp }
  return { ...normalType, meta }
}

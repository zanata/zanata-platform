
import { merge } from 'lodash'

/**
 * Wrap an API call descriptor with this to make it a GET request that includes
 * credentials and specifies JSON content type headers.
 *
 * e.g.
 *
 * [CALL_API]: getJsonWithCredentials({
 *   endpoint: myUrl,
 *   method: 'GET',
 *   ...
 *   })
 *
 * @param apiCall a descriptor that is to be bound to action.[CALL_API]
 * @see redux-api-middleware
 */
export function getJsonWithCredentials (apiCall) {
  return merge({}, apiCall, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    }
  })
}

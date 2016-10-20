/**
 * Encode a document id for use in a REST URL.
 *
 * Replaces '/' with ','
 *
 * @param docId
 * @returns {*}
 */
export function encode (docId) {
  return docId ? docId.replace(/\//g, ',') : docId
}

/**
 * Decode an encoded document id from a REST URL.
 *
 * Replaces ',' with '/'
 *
 * @param docId
 * @returns {*}
 */
export function decode (docId) {
  return docId ? docId.replace(/,/g, '/') : docId
}

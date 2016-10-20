export function startsWith (str, prefix, ignoreCase) {
  if (ignoreCase && str && prefix) {
    str = str.toUpperCase()
    prefix = prefix.toUpperCase()
  }
  return str.lastIndexOf(prefix, 0) === 0
}

export function endsWith (str, suffix, ignoreCase) {
  if (ignoreCase && str && suffix) {
    str = str.toUpperCase()
    suffix = suffix.toUpperCase()
  }
  return str.indexOf(suffix, str.length - suffix.length) !== -1
}

export function equals (from, to, ignoreCase) {
  if (ignoreCase && from && to) {
    from = from.toUpperCase()
    to = to.toUpperCase()
  }
  return from === to
}

/**
 * Template tag function to allow single-line template strings to be wrapped.
 *
 * Removes all newlines and leading whitespace from the template string.
 */
export function oneLiner (strings, ...vars) {
  // interleave strings and vars
  var output = ''
  for (let i = 0; i < vars.length; i++) {
    output += strings[i] + vars[i]
  }
  output += strings[vars.length]
  const lines = output.split(/\n/)
  return lines.map((line) => {
    return line.replace(/^\s+/gm, '')
  }).join(' ').trim()
}

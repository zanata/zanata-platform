import React, { PropTypes } from 'react'

/**
 * Displays the set of key combinations for a keyboard shortcut.
 */
function KeyCombinations ({ keys }) {
  const keyDefs = keys.map(symbolizeKey).map((key, index) => {
    return <kbd key={index}>{key}</kbd>
  })
  return <span>{keyDefs}</span>
}

KeyCombinations.propTypes = {
  // string representations of keyboard shortcuts, in long form (not symbolized)
  keys: PropTypes.arrayOf(PropTypes.string).isRequired
}

export default KeyCombinations

// symbols to use for named keys
const symbols = {
  command: '⌘',
  shift: '⇧',
  left: '←',
  right: '→',
  up: '↑',
  down: '↓',
  'return': '↩',
  backspace: '⌫'
}

/**
 * Convert strings like cmd into symbols like ⌘
 * @param  {String} combo Key combination, e.g. 'mod+f'
 * @return {String} The key combination with symbols
 */
function symbolizeKey (combo) {
  combo = combo.split('+')

  for (var i = 0; i < combo.length; i++) {
    // try to resolve command / ctrl based on OS:
    if (combo[i] === 'mod') {
      if (window.navigator &&
        window.navigator.platform.indexOf('Mac') >= 0) {
        combo[i] = 'command'
      } else {
        combo[i] = 'ctrl'
      }
    }
    combo[i] = symbols[combo[i]] || combo[i]
  }

  return combo.join(' + ')
}
